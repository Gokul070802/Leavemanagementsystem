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

    function getUser() {
        try {
            return JSON.parse(localStorage.getItem('loggedInUser'));
        } catch (e) {
            return null;
        }
    }

    function isAuthenticated(user) {
        return !!(user && user.role && user.role !== 'guest' && user.token);
    }

    var page = (window.location.pathname.split('/').pop() || 'index.html').toLowerCase();
    var publicPages = ['index.html', 'forgot-password.html', ''];
    var isPublic = publicPages.indexOf(page) !== -1;

    var currentUser = getUser();

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

    // bfcache guard: fires when the browser restores a page from back/forward cache
    // DOMContentLoaded does NOT fire in this case — only pageshow does.
    window.addEventListener('pageshow', function (event) {
        if (!event.persisted) return;
        var u = getUser();
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
