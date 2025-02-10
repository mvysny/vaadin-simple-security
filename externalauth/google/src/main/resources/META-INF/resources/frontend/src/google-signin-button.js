class GoogleSigninButton extends HTMLElement {
    connectedCallback() {
        google.accounts.id.initialize({
            client_id: this.client_id,
            callback: this.handleCredentialResponse.bind(this),
            cancel_on_tap_outside: this.cancel_on_tap_outside,
            context: this.context,
            itp_support: this.itp_support,
            login_hint: this.login_hint,
        });
        google.accounts.id.renderButton(this, {
            type: this.button_type,
            theme: this.button_theme,
            size: this.button_size,
            shape: this.button_shape,
            text: this.button_text,
            logo_alignment: this.logo_alignment,
        });
        google.accounts.id.prompt();
    }
    handleCredentialResponse(response) {
        this.$server.onSignIn(response.credential);
    }
}
window.customElements.define('google-signin-button', GoogleSigninButton);
