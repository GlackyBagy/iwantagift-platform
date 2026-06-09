document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");

    if (!form) return;

    const emailInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");

    const errorBlock = document.getElementById("validationError");
    const submitButton = form.querySelector(".btn-auth[type='submit']");
    const serverErrorAlert = document.getElementById("loginError");
    const inputs = [emailInput, passwordInput].filter(Boolean);
    const touchedInputs = new Set();
    let submitAttempted = false;

    function hasValidRequiredFields() {
        if (!emailInput || !passwordInput) return true;

        return isValidEmail(emailInput.value.trim()) &&
            passwordInput.value.length >= 8;
    }

    function setSubmitState(isValid = hasValidRequiredFields()) {
        if (!submitButton || !emailInput || !passwordInput) return;

        submitButton.disabled = !isValid;
    }

    function showError(message, ...inputs) {
        if (errorBlock) {
            errorBlock.textContent = message;
            errorBlock.classList.remove("d-none");
        }

        inputs.forEach(i => i && i.classList.add("is-invalid"));
    }

    function clearError() {
        if (errorBlock) {
            errorBlock.textContent = "";
            errorBlock.classList.add("d-none");
        }
    }

    function clearInvalid(...inputs) {
        inputs.forEach(i => i && i.classList.remove("is-invalid"));
    }

    function hideServerError() {
        if (serverErrorAlert) {
            serverErrorAlert.classList.add("d-none");
        }
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function shouldShowError(...inputs) {
        return submitAttempted || inputs.some(input => input && touchedInputs.has(input));
    }

    function validateForm(showTouchedErrors = true) {
        const email = emailInput.value.trim();
        const password = passwordInput.value;

        clearError();
        clearInvalid(emailInput, passwordInput);

        if (!isValidEmail(email)) {
            setSubmitState(false);
            if (showTouchedErrors && shouldShowError(emailInput)) {
                showError("Enter a valid email address.", emailInput);
            }
            return false;
        }

        if (password.length < 8) {
            setSubmitState(false);
            if (showTouchedErrors && shouldShowError(passwordInput)) {
                showError("Password must be at least 8 characters long.", passwordInput);
            }
            return false;
        }

        setSubmitState(true);
        return true;
    }

    setSubmitState();

    inputs.forEach(input => {
        input.addEventListener("input", () => {
            touchedInputs.add(input);
            hideServerError();
            clearInvalid(emailInput, passwordInput);
            validateForm();
        });
    });

    form.addEventListener("submit", (event) => {
        submitAttempted = true;
        if (!validateForm()) {
            event.preventDefault();
        }
    });
});
