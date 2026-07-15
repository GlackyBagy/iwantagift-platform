// Handles destructive actions in the danger zone with confirmation
(function () {
    "use strict";

    function getCsrfToken() {
        const meta = document.querySelector("meta[name='_csrf']");
        return meta ? meta.getAttribute("content") : null;
    }

    function logout(csrfToken) {
        const headers = { Accept: "application/json" };
        if (csrfToken) {
            headers["X-CSRF-TOKEN"] = csrfToken;
        }
        return fetch("/logout", {
            method: "POST",
            headers: headers,
            credentials: "same-origin",
        });
    }

    document.addEventListener("DOMContentLoaded", () => {
        const csrfToken = getCsrfToken();
        const buttons = document.querySelectorAll(".settings-danger-button");

        buttons.forEach((button) => {
            const article = button.closest(".settings-danger-action");
            if (!article) return;

            const heading = article.querySelector("h3");
            if (!heading) return;

            let action = null;
            let confirmMessage = null;
            let endpoint = null;

            if (heading.textContent.includes("Delete account")) {
                action = "delete-account";
                confirmMessage = "Are you sure you want to delete your account? This action is irreversible.";
                endpoint = "/settings/deleteAccount";
            } else if (heading.textContent.includes("Delete all wishes")) {
                action = "delete-all";
                confirmMessage = "Are you sure you want to delete all wishlists and wishes? This action is irreversible.";
                endpoint = "/settings/deleteAll";
            }

            if (!action || !endpoint) return;

            button.disabled = false;
            button.addEventListener("click", (e) => {
                e.preventDefault();

                if (!confirm(confirmMessage)) {
                    return;
                }

                button.disabled = true;
                const originalText = button.textContent;
                button.textContent = "Processing…";

                const url = new URL(endpoint, window.location.origin);
                if (csrfToken) {
                    url.searchParams.append("_csrf", csrfToken);
                }

                fetch(url, {
                    method: "POST",
                    headers: {
                        Accept: "application/json",
                    },
                    credentials: "same-origin",
                })
                    .then((response) => {
                        if (!response.ok) {
                            throw new Error(`Request failed with status ${response.status}`);
                        }
                        alert("Confirmation email has been sent to your email address. Please check your inbox.");
                        if (action === "delete-account") {
                            return logout(csrfToken).then(() => {
                                window.location.assign("/");
                            });
                        }
                        return response.text();
                    })
                    .catch((error) => {
                        console.error(`${action} request failed:`, error);
                        button.disabled = false;
                        button.textContent = originalText;
                        alert("Action failed. Please try again later.");
                    });
            });
        });
    });
})();
