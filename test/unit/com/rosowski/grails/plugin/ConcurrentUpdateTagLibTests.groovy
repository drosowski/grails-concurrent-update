package com.rosowski.grails.plugin

import grails.test.TagLibUnitTestCase
import org.hibernate.Session

class ConcurrentUpdateTagLibTests extends TagLibUnitTestCase {

  protected ConcurrentUpdateTagLib tagLib

  protected void setUp() {
    super.setUp()
    ConcurrentUpdateTagLib.metaClass.message = { Map params -> return "message" }
    tagLib = new ConcurrentUpdateTagLib()
    tagLib.grailsApplication = [isDomainClass: { def value -> return false}]
  }

  protected void tearDown() {
    super.tearDown()
  }

  void testStoredValue() {
    CheckedClass checked = new CheckedClass(name: "foobar", version: 1)
    mockDomain(CheckedClass, [checked])
    checked.metaClass.isLocked = true
    def newChecked = new CheckedClass(name: "foobar", version: 2)
    newChecked.metaClass.name_persisted = "barfoo"
    tagLib.concurrentUpdateService = [getPersistentValues: { def bean -> return newChecked }]

    Map attrs = [field: "name", bean: checked]
    def result = tagLib.storedValue(attrs, null).toString()
    assertTrue(result.contains("barfoo"))
  }
}
