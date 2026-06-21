document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("signupForm");

    if (!form) return;

    const nicknameInput = form.querySelector('input[name="nickname"]');
    const emailInput = form.querySelector('input[name="email"]');
    const passwordInput = document.getElementById("password");
    const confirmPasswordInput = document.getElementById("confirmPassword");

    const errorBlock = document.getElementById("validationError");
    const submitButton = form.querySelector(".btn-auth[type='submit']");
    const emailFeedback = emailInput
        ? emailInput.closest(".mb-3")?.querySelector(".invalid-feedback")
        : null;
    const inputs = [nicknameInput, emailInput, passwordInput, confirmPasswordInput].filter(Boolean);
    const touchedInputs = new Set();
    const initialEmailError = emailFeedback ? emailFeedback.textContent.trim() : "";
    const initialEmailWasRejected = emailInput &&
        emailInput.classList.contains("is-invalid") &&
        initialEmailError.toLowerCase().includes("exists");
    const takenEmailMessage = initialEmailWasRejected ? initialEmailError : "Email already exists";
    const takenEmail = initialEmailWasRejected ? normalizeEmail(emailInput.value) : null;
    let submitAttempted = false;

    function hasValidRequiredFields() {
        if (!emailInput || !passwordInput) return true;

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        if (!isValidEmail(email) || password.length < 8) return false;
        if (isTakenEmail(email)) return false;

        if (nicknameInput && nicknameInput.value.trim().length < 1) return false;

        return !(confirmPasswordInput && password !== confirmPasswordInput.value);
    }

    function setSubmitState(isValid = hasValidRequiredFields()) {
        if (!submitButton || !emailInput || !passwordInput) return;

        submitButton.disabled = !isValid;
    }

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

    function normalizeEmail(email) {
        return email.trim().toLowerCase();
    }

    function isTakenEmail(email) {
        return takenEmail !== null && normalizeEmail(email) === takenEmail;
    }

    function showEmailTakenError() {
        emailInput.classList.add("is-invalid");

        if (!emailFeedback) {
            showError(takenEmailMessage, emailInput);
            return;
        }

        emailFeedback.textContent = takenEmailMessage;
        emailFeedback.style.display = "block";
    }

    function hideEmailFeedback() {
        if (emailFeedback) {
            emailFeedback.style.display = "none";
            emailFeedback.classList.remove("d-block");
        }
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function shouldShowError(...inputs) {
        return submitAttempted || inputs.some(input => input && touchedInputs.has(input));
    }

    function validateForm(showTouchedErrors = true) {
        const nickname = nicknameInput ? nicknameInput.value.trim() : "";
        const email = emailInput.value.trim();
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput ? confirmPasswordInput.value : "";

        clearError();
        clearInvalid(nicknameInput, emailInput, passwordInput, confirmPasswordInput);
        hideEmailFeedback();

        if (nicknameInput && nickname.length < 1) {
            setSubmitState(false);
            if (showTouchedErrors && shouldShowError(nicknameInput)) {
                showError("Username must contain at least 1 character.", nicknameInput);
            }
            return false;
        }

        if (!isValidEmail(email)) {
            setSubmitState(false);
            if (showTouchedErrors && shouldShowError(emailInput)) {
                showError("Enter a valid email address.", emailInput);
            }
            return false;
        }

        if (isTakenEmail(email)) {
            setSubmitState(false);
            showEmailTakenError();
            return false;
        }

        if (password.length < 8) {
            setSubmitState(false);
            if (showTouchedErrors && shouldShowError(passwordInput)) {
                showError("Password must be at least 8 characters long.", passwordInput);
            }
            return false;
        }

        if (confirmPasswordInput && password !== confirmPassword) {
            setSubmitState(false);
            if (showTouchedErrors && shouldShowError(confirmPasswordInput)) {
                showError("Passwords do not match.", passwordInput, confirmPasswordInput);
            }
            return false;
        }

        setSubmitState(true);
        return true;
    }

    setSubmitState();
    if (initialEmailWasRejected) {
        showEmailTakenError();
    }

    inputs.forEach(input => {
        input.addEventListener("input", () => {
            touchedInputs.add(input);
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
