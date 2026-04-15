(function () {
    "use strict";

    function slugify(value) {
        return String(value || "item")
            .toLowerCase()
            .replace(/[^a-z0-9]+/g, "-")
            .replace(/^-+|-+$/g, "")
            .replace(/-{2,}/g, "-");
    }

    function preferredName(el) {
        return el.getAttribute("name")
            || el.getAttribute("aria-label")
            || el.getAttribute("placeholder")
            || el.getAttribute("title")
            || (el.textContent || "").trim().slice(0, 30)
            || el.getAttribute("type")
            || el.tagName.toLowerCase();
    }

    function currentPageKey() {
        var path = window.location.pathname || "page";
        var file = path.split("/").pop() || "page";
        return slugify(file.replace(/\.html$/i, "")) || "page";
    }

    function ensureIdAndTestId(elements, pageKey, kind) {
        elements.forEach(function (el, index) {
            if (!el.id) {
                el.id = slugify(pageKey + "-" + kind + "-" + preferredName(el) + "-" + (index + 1));
            }
            if (!el.getAttribute("data-testid")) {
                el.setAttribute("data-testid", el.id);
            }
            if (!el.getAttribute("data-sel")) {
                el.setAttribute("data-sel", kind);
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        var pageKey = currentPageKey();

        if (document.body) {
            if (!document.body.id) {
                document.body.id = pageKey + "-page";
            }
            if (!document.body.getAttribute("data-testid")) {
                document.body.setAttribute("data-testid", document.body.id);
            }
            if (!document.body.getAttribute("data-page")) {
                document.body.setAttribute("data-page", pageKey);
            }
        }

        ensureIdAndTestId(Array.from(document.querySelectorAll("form")), pageKey, "form");
        ensureIdAndTestId(Array.from(document.querySelectorAll("input, select, textarea")), pageKey, "field");
        ensureIdAndTestId(Array.from(document.querySelectorAll("button, [role='button']")), pageKey, "button");
        ensureIdAndTestId(Array.from(document.querySelectorAll("a")), pageKey, "link");
        ensureIdAndTestId(Array.from(document.querySelectorAll("table")), pageKey, "table");

        Array.from(document.querySelectorAll("label")).forEach(function (label, index) {
            if (label.getAttribute("for")) {
                return;
            }
            var nearbyField = label.parentElement
                ? label.parentElement.querySelector("input, select, textarea")
                : null;
            if (nearbyField && nearbyField.id) {
                label.setAttribute("for", nearbyField.id);
            } else if (nearbyField) {
                var generated = slugify(pageKey + "-field-label-target-" + (index + 1));
                nearbyField.id = nearbyField.id || generated;
                label.setAttribute("for", nearbyField.id);
            }
            if (!label.getAttribute("data-testid")) {
                label.setAttribute("data-testid", slugify(pageKey + "-label-" + preferredName(label) + "-" + (index + 1)));
            }
        });
    });
})();