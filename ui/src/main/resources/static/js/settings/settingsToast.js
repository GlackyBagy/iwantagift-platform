(function () {
    "use strict";

    const MESSAGES = {
        "flash-emailChangeSent":    "Confirmation link sent — check your inbox to verify the new email.",
        "flash-passwordChangeSent": "Confirmation link sent — check your inbox to verify the password change.",
        "flash-emailConfirmSent":   "Confirmation link sent — check your inbox to verify your email.",
    };

    const AUTO_DISMISS_MS = 6000;

    function createContainer() {
        const container = document.createElement("div");
        container.className = "settings-toast-container";
        document.body.appendChild(container);
        return container;
    }

    function showToast(container, message) {
        const toast = document.createElement("div");
        toast.className = "settings-toast";
        toast.setAttribute("role", "status");
        toast.innerHTML = `
            <span class="settings-toast-icon">✉</span>
            <span>${message}</span>
            <button class="settings-toast-close" aria-label="Dismiss">&times;</button>
        `;

        const close = toast.querySelector(".settings-toast-close");

        function dismiss() {
            toast.classList.remove("is-visible");
            toast.addEventListener("transitionend", () => toast.remove(), { once: true });
        }

        close.addEventListener("click", dismiss);
        setTimeout(dismiss, AUTO_DISMISS_MS);

        container.appendChild(toast);
        requestAnimationFrame(() => {
            requestAnimationFrame(() => toast.classList.add("is-visible"));
        });
    }

    document.addEventListener("DOMContentLoaded", () => {
        const flashes = Object.keys(MESSAGES).filter(id => document.getElementById(id));
        if (flashes.length === 0) return;

        const container = createContainer();
        flashes.forEach(id => showToast(container, MESSAGES[id]));
    });
})();
