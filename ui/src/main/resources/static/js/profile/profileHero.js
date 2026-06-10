// Hydrates the profile-page hero (avatar / nickname / description) from /api/profile/{ownerId}
// after the page shell has rendered. The owner id is read from the hero's data attribute, so the
// same script works for both the own and foreign profile pages. If the profile service is down the
// hero degrades to a neutral placeholder instead of failing the page (wishlists are independent).
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", () => {
        const hero = document.getElementById("profileHero");
        if (!hero) return;

        const ownerId = hero.dataset.profileOwnerId;
        if (!ownerId) return;

        const avatar = document.getElementById("profileAvatar");
        const nickname = document.getElementById("profileNickname");
        const description = document.getElementById("profileDescription");

        function markLoaded() {
            hero.classList.remove("is-loading");
        }

        fetch("/api/profile/" + encodeURIComponent(ownerId), {
            headers: { Accept: "application/json" },
            credentials: "same-origin",
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Profile request failed with status " + response.status);
                }
                return response.json();
            })
            .then((profile) => {
                if (nickname) nickname.textContent = profile.nickname ?? "User";
                if (description) description.textContent = profile.description ?? "";
                if (avatar && profile.avatarUrl) avatar.src = profile.avatarUrl;
                markLoaded();
            })
            .catch((error) => {
                console.warn("Profile hero hydration failed:", error);
                if (nickname) nickname.textContent = "User";
                if (description) description.textContent = "Profile is temporarily unavailable.";
                hero.classList.add("is-unavailable");
                markLoaded();
            });
    });
})();
