// Hydrates the profile settings block from the /api/profile BFF endpoint after the page shell has
// loaded. On success it fills the fields and enables editing; on failure (profile service down) it
// leaves the whole block disabled and shows a notice, without affecting the rest of the page.
(function () {
    "use strict";

    const PROFILE_ENDPOINT = "/api/profile";

    document.addEventListener("DOMContentLoaded", () => {
        const card = document.getElementById("profileSettingsCard");
        if (!card) return;

        const status = document.getElementById("profileSettingsStatus");
        const note = document.getElementById("profileUnavailableNote");
        const avatar = document.getElementById("settingsAvatar");
        const nickname = document.getElementById("settings-nickname");
        const description = document.getElementById("settings-description");

        // Every input/select/textarea/button inside the card is toggled together.
        const controls = card.querySelectorAll("input, textarea, select, button");

        function setControlsDisabled(disabled) {
            controls.forEach((el) => {
                el.disabled = disabled;
            });
        }

        function markLoaded() {
            card.classList.remove("is-loading");
            card.removeAttribute("aria-busy");
        }

        function hydrate(profile) {
            if (nickname) nickname.value = profile.nickname ?? "";
            if (description) description.value = profile.description ?? "";
            if (avatar && profile.avatarUrl) avatar.src = profile.avatarUrl;

            setControlsDisabled(false);
            if (status) status.textContent = "Visible";
            if (note) note.hidden = true;
            markLoaded();
        }

        function disableBlock() {
            setControlsDisabled(true);
            card.classList.add("is-unavailable");
            if (status) status.textContent = "Unavailable";
            if (note) note.hidden = false;
            markLoaded();
        }

        fetch(PROFILE_ENDPOINT, {
            headers: { Accept: "application/json" },
            credentials: "same-origin",
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Profile request failed with status " + response.status);
                }
                return response.json();
            })
            .then(hydrate)
            .catch((error) => {
                console.warn("Profile hydration failed:", error);
                disableBlock();
            });
    });
})();
