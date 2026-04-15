window.LEAVEPAL_ENV = window.LEAVEPAL_ENV || {};

// Set this in each deployment environment.
// Example production value: "https://your-backend-domain.com"
(function () {
	var currentValue = window.LEAVEPAL_ENV.API_BASE_URL || "";
	if (currentValue) {
		return;
	}

	var hostname = window.location.hostname || "";
	var port = window.location.port || "";
	var isLocalhost = hostname === "localhost" || hostname === "127.0.0.1";

	// When static pages are opened from a local dev server such as Live Server,
	// route API traffic to the Spring Boot backend on 8081 by default.
	if (isLocalhost && port && port !== "8081") {
		window.LEAVEPAL_ENV.API_BASE_URL = "http://localhost:8081";
		return;
	}

	// In production the frontend is served by the same Spring Boot service,
	// so API calls can be relative (same-origin). Leave URL empty.
	window.LEAVEPAL_ENV.API_BASE_URL = "";
})();
