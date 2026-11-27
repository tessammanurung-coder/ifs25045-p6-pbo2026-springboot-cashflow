package org.delcom.ifs25045springbootcashflow.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.NotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotFoundViewTest {

    @BeforeEach
    void setUp() {
        MockVaadin.setup(new Routes().autoDiscoverViews(
                "org.delcom.ifs25045springbootcashflow.framework.view"));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    void returns404WithTitleAndMainLandmark() {
        NotFoundView view = new NotFoundView();
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getLocation()).thenReturn(new Location("about"));

        int status = view.setErrorParameter(event, new ErrorParameter<>(NotFoundException.class, new NotFoundException()));

        assertEquals(HttpServletResponse.SC_NOT_FOUND, status);
        assertEquals("Halaman Tidak Ditemukan", view.getPageTitle());
        assertEquals("main", view.getElement().getAttribute("role"));
        assertTrue(view.getChildren().anyMatch(c -> c.getElement().getTag().equals("h1")));
    }
}