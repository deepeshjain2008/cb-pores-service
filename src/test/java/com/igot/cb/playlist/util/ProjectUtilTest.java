package com.igot.cb.playlist.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class ProjectUtilTest {

  @Test
  void constructor_isPrivateAndThrows() throws Exception {
    Constructor<ProjectUtil> constructor = ProjectUtil.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    InvocationTargetException exception =
        assertThrows(InvocationTargetException.class, constructor::newInstance);

    assertEquals(IllegalStateException.class, exception.getCause().getClass());
    assertEquals("Utility class", exception.getCause().getMessage());
  }
}
