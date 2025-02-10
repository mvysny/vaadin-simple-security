class GoogleSigninButton extends HTMLElement {
    connectedCallback() {
        google.accounts.id.initialize({client_id: this.clientId, callback: this.handleCredentialResponse.bind(this)});
        google.accounts.id.renderButton(this, {theme:'outline', size:'large'});
        google.accounts.id.prompt();
    }
    handleCredentialResponse(response) {
        this.$server.onSignIn(response.credential);
    }
}
window.customElements.define('google-signin-button', GoogleSigninButton);
