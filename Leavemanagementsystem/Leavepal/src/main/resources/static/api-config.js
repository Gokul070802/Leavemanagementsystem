(function () {
    function normalizeBaseUrl(value) {
        return String(value || "").trim().replace(/\/+$/, "");
    }

    function resolveApiBaseUrl() {
        var fromWindow = window.LEAVEPAL_ENV && window.LEAVEPAL_ENV.API_BASE_URL;
        var fromStorage = "";
        try {
            fromStorage = localStorage.getItem("LEAVEPAL_API_BASE_URL") || "";
        } catch (error) {
            fromStorage = "";
        }

        return normalizeBaseUrl(fromWindow || fromStorage);
    }

    var apiBaseUrl = resolveApiBaseUrl();
    var nativeFetch = window.fetch.bind(window);
    var inflightGetRequests = new Map();

    window.LEAVEPAL_API_BASE_URL = apiBaseUrl;
    window.buildApiUrl = function (path) {
        if (!path) {
            return apiBaseUrl;
        }

        var value = String(path);
        if (/^https?:\/\//i.test(value)) {
            return value;
        }

        if (!value.startsWith("/")) {
            value = "/" + value;
        }

        return apiBaseUrl + value;
    };

    window.fetch = function (input, init) {
        var requestUrl = "";
        var method = "GET";

        if (typeof input === "string") {
            requestUrl = input;
            method = (init && init.method ? String(init.method) : "GET").toUpperCase();
        } else if (input instanceof Request) {
            requestUrl = input.url || "";
            method = (init && init.method ? String(init.method) : input.method || "GET").toUpperCase();
        }

        var isApiRequest = requestUrl.startsWith("/api/");
        if (!isApiRequest && requestUrl.indexOf(apiBaseUrl + "/api/") === 0) {
            isApiRequest = true;
        }

        if (!isApiRequest) {
            return nativeFetch(input, init);
        }

        var resolvedUrl = requestUrl.startsWith("/api/") ? window.buildApiUrl(requestUrl) : requestUrl;
        if (method !== "GET") {
            if (input instanceof Request && requestUrl.startsWith("/api/")) {
                return nativeFetch(new Request(resolvedUrl, input), init);
            }
            return nativeFetch(resolvedUrl, init);
        }

        var dedupeKey = method + "::" + resolvedUrl;
        if (inflightGetRequests.has(dedupeKey)) {
            return inflightGetRequests.get(dedupeKey).then(function (response) {
                return response.clone();
            });
        }

        var fetchPromise;
        if (input instanceof Request) {
            fetchPromise = nativeFetch(new Request(resolvedUrl, input), init);
        } else {
            fetchPromise = nativeFetch(resolvedUrl, init);
        }

        var trackedPromise = fetchPromise.then(function (response) {
            inflightGetRequests.delete(dedupeKey);
            return response;
        }).catch(function (error) {
            inflightGetRequests.delete(dedupeKey);
            throw error;
        });

        inflightGetRequests.set(dedupeKey, trackedPromise);
        return trackedPromise.then(function (response) {
            return response.clone();
        });
    };
})();
