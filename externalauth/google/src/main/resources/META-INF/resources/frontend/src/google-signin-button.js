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
        google.accounts.id.renderButton(this, {theme:'outline', size:'large'});
        google.accounts.id.prompt();
    }
    handleCredentialResponse(response) {
        this.$server.onSignIn(response.credential);
    }
}
window.customElements.define('google-signin-button', GoogleSigninButton);
