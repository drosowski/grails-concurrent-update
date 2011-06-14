package com.rosowski.grails.plugin

import grails.test.TagLibUnitTestCase
import org.hibernate.Session

class ConcurrentUpdateTagLibTests extends TagLibUnitTestCase {

  protected ConcurrentUpdateTagLib tagLib

  protected void setUp() {
    super.setUp()
    ConcurrentUpdateTagLib.metaClass.message = { Map params -> return "${params.args[0]}" }
    tagLib = new ConcurrentUpdateTagLib()
  }

  protected void tearDown() {
    super.tearDown()
  }

  void testStoredValue() {
    CheckedClass checked = new CheckedClass(name: "foobar", version: 1)
    mockDomain(CheckedClass, [checked])
    checked.metaClass.static.withSession = { def closure ->
      Map sessionMap = [setFlushMode: { org.hibernate.FlushMode mode -> }]
      sessionMap.get = { String bean, Long id -> return new CheckedClass(name: "barfoo", version: 2) }
      Session session = sessionMap as Session
      closure.call(session)
    }

    checked.metaClass.getPersistentValue = { String field ->
      if (field == "version") {
        return 1
      }
      else {
        return "foobar"
      }
    }

    Map attrs = [field: "name", bean: checked]
    def result = tagLib.storedValue(attrs, null).toString()
    assertTrue(result.contains("barfoo"))
  }
}
