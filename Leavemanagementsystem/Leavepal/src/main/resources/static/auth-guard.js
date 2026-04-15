/**
 * auth-guard.js
 * Must be the FIRST script included on every page.
 * - Immediately redirects unauthenticated users from protected pages to login.
 * - Redirects already-authenticated users away from the login page.
 * - Handles browser back/forward (bfcache) bypass by re-checking auth on pageshow.
 * - Sets data-user-role on <html> so CSS can toggle manager-only nav items instantly.
 */
(function () {
    'use strict';

    function clearAuthState() {
        try {
            localStorage.removeItem('loggedInUser');
            localStorage.removeItem('userRole');
            localStorage.removeItem('loginRoleTab');
            sessionStorage.setItem('leavepal-logout-ts', String(Date.now()));
        } catch (e) {
            // Ignore storage failures in private/locked-down browser modes
        }
    }

    function decodeTokenPayload(token) {
        try {
            var parts = String(token || '').split('.');
            if (parts.length !== 3) {
                return null;
            }
            var b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
            var json = decodeURIComponent(atob(b64).split('').map(function (c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(json);
        } catch (e) {
            return null;
        }
    }

    function isTokenExpired(token) {
        var payload = decodeTokenPayload(token);
        if (!payload || typeof payload.exp !== 'number') {
            return false;
        }
        return Date.now() >= payload.exp * 1000;
    }

    function getUser() {
        try {
            return JSON.parse(localStorage.getItem('loggedInUser'));
        } catch (e) {
            return null;
        }
    }

    function isAuthenticated(user) {
        return !!(user && user.role && user.role !== 'guest' && user.token && !isTokenExpired(user.token));
    }

    window.leavepalSecureLogout = function () {
        clearAuthState();
        if (window.caches && typeof window.caches.keys === 'function') {
            window.caches.keys().then(function (keys) {
                return Promise.all(keys.map(function (key) {
                    return window.caches.delete(key);
                }));
            }).catch(function () {
                // Ignore cache API failures
            });
        }
        window.location.replace('index.html');
    };

    var page = (window.location.pathname.split('/').pop() || 'index.html').toLowerCase();
    var publicPages = ['index.html', 'forgot-password.html', ''];
    var isPublic = publicPages.indexOf(page) !== -1;

    var currentUser = getUser();
    if (currentUser && currentUser.token && isTokenExpired(currentUser.token)) {
        clearAuthState();
        currentUser = null;
    }

    // Set data-user-role on <html> immediately so CSS can react before first paint
    if (currentUser && currentUser.role) {
        document.documentElement.setAttribute('data-user-role', (currentUser.role || 'employee').toLowerCase());
    }

    // Synchronous guard: redirect unauthenticated visitors away from protected pages
    if (!isPublic && !isAuthenticated(currentUser)) {
        window.location.replace('index.html');
    }

    // Redirect already-authenticated users away from the login page
    if (page === 'index.html' && isAuthenticated(currentUser)) {
        var dest = (currentUser.role || '').toLowerCase() === 'admin'
            ? 'admin-dashboard.html'
            : 'dashboard.html';
        window.location.replace(dest);
    }

    if (!isPublic) {
        window.history.pushState({ leavepalProtected: true }, '', window.location.href);
        window.addEventListener('popstate', function () {
            window.leavepalSecureLogout();
        });
    }

    // bfcache guard: fires when the browser restores a page from back/forward cache
    // DOMContentLoaded does NOT fire in this case — only pageshow does.
    window.addEventListener('pageshow', function () {
        var u = getUser();
        if (u && u.token && isTokenExpired(u.token)) {
            clearAuthState();
            u = null;
        }
        if (!isPublic && !isAuthenticated(u)) {
            window.location.replace('index.html');
            return;
        }
        if (page === 'index.html' && isAuthenticated(u)) {
            var d = (u.role || '').toLowerCase() === 'admin'
                ? 'admin-dashboard.html'
                : 'dashboard.html';
            window.location.replace(d);
        }
    });
}());
