document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("signupForm");

    if (!form) return;

    const usernameInput = form.querySelector('input[name="username"]');
    const emailInput = form.querySelector('input[name="email"]');
    const passwordInput = document.getElementById("password");
    const confirmPasswordInput = document.getElementById("confirmPassword");

    const errorBlock = document.getElementById("validationError");

    function showError(message, ...inputs) {
        errorBlock.textContent = message;
        errorBlock.classList.remove("d-none");

        inputs.forEach(i => i && i.classList.add("is-invalid"));
    }

    function clearError() {
        errorBlock.textContent = "";
        errorBlock.classList.add("d-none");
    }

    function clearInvalid(...inputs) {
        inputs.forEach(i => i && i.classList.remove("is-invalid"));
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function validateForm() {
        const username = usernameInput.value.trim();
        const email = emailInput.value.trim();
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        clearError();
        clearInvalid(usernameInput, emailInput, passwordInput, confirmPasswordInput);

        if (username.length < 1) {
            showError("Username must contain at least 1 character.", usernameInput);
            return false;
        }

        if (!isValidEmail(email)) {
            showError("Enter a valid email address.", emailInput);
            return false;
        }

        if (password.length < 8) {
            showError("Password must be at least 8 characters long.", passwordInput, confirmPasswordInput);
            return false;
        }

        if (password !== confirmPassword) {
            showError("Passwords do not match.", passwordInput, confirmPasswordInput);
            return false;
        }

        return true;
    }

    form.addEventListener("input", validateForm);

    form.addEventListener("submit", (event) => {
        if (!validateForm()) {
            event.preventDefault();
        }
    });
});