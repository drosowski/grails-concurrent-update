package com.rosowski.grails.plugin

import grails.test.GrailsUnitTestCase

class CheckedClassTests extends GrailsUnitTestCase {
  protected void setUp() {
    super.setUp()
  }

  protected void tearDown() {
    super.tearDown()
  }

  void testConstraints() {
    // optimistic locking
    CheckedClass checked = new CheckedClass(name: 'foobar', version: 1)
    mockForConstraintsTests(CheckedClass, [checked])
    checked.metaClass.getPersistentValue = { String field -> return 1 }

    checked.version = 0
    checked.name = "barfoo"
    assertFalse("Invalid customer could be validated", checked.validate())
    assertEquals("Violated constraint not detected", "optimistic.locking.failure", checked.errors.version)
  }
}
