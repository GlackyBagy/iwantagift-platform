(function () {
    function readCookie(name) {
        return document.cookie
            .split("; ")
            .find((cookie) => cookie.startsWith(name + "="))
            ?.substring(name.length + 1);
    }

    function setElementValue(selector, value) {
        const element = document.querySelector(selector);
        if (!element) {
            return;
        }

        if (element instanceof HTMLInputElement || element instanceof HTMLTextAreaElement) {
            element.value = value;
            return;
        }

        element.textContent = value;
    }

    async function loadProfile(container) {
        const {
            profileOwnerId,
            profileApiBaseUrl,
            accessCookieName,
            defaultProfileNickname,
            defaultProfileDescription,
            nicknameTarget,
            descriptionTarget,
            avatarTarget
        } = container.dataset;

        const accessToken = readCookie(accessCookieName);
        const headers = accessToken ? {"Authorization": "Bearer " + decodeURIComponent(accessToken)} : {};
        const response = await fetch(
            profileApiBaseUrl + "/api/v1/profile?profileId=" + encodeURIComponent(profileOwnerId),
            {headers}
        );

        if (!response.ok) {
            return;
        }

        const profile = await response.json();
        setElementValue(nicknameTarget, profile.nickname || defaultProfileNickname || "User");
        setElementValue(descriptionTarget, profile.description || defaultProfileDescription || "");
        if (profile.hasAvatar && avatarTarget) {
            const avatar = document.querySelector(avatarTarget);
            if (avatar instanceof HTMLImageElement) {
                avatar.src = profileApiBaseUrl + "/api/v1/profile/" + encodeURIComponent(profileOwnerId) + "/avatar";
            }
        }
    }

    document.addEventListener("DOMContentLoaded", () => {
        document
            .querySelectorAll("[data-profile-loader]")
            .forEach((container) => loadProfile(container).catch(console.error));
    });
})();
