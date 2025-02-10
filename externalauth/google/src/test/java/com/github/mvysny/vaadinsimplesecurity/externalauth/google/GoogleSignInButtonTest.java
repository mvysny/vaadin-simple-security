package com.github.mvysny.vaadinsimplesecurity.externalauth.google;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoogleSignInButtonTest {
    @BeforeEach public void fakeVaadin() {
        MockVaadin.setup();
    }
    @AfterEach
    public void tearDownVaadin() {
        MockVaadin.tearDown();
    }
    @Test
    public void smoke() {
        UI.getCurrent().add(new GoogleSignInButton());
    }
}