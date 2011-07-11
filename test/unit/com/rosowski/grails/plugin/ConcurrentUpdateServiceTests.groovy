package com.rosowski.grails.plugin

import grails.test.GrailsUnitTestCase
import org.hibernate.Session
import org.codehaus.groovy.grails.commons.GrailsDomainClass

class ConcurrentUpdateServiceTests extends GrailsUnitTestCase {

  protected ConcurrentUpdateService concurrentUpdateService

  protected void setUp() {
    super.setUp()
    concurrentUpdateService = new ConcurrentUpdateService()
  }

  protected void tearDown() {
    super.tearDown()
  }

  void testGetPersistentValues() {
    CheckedClass checked = new CheckedClass(name: "foobar", version: 1)
    mockDomain(CheckedClass, [checked])
    checked.metaClass.isLocked = true
    checked.metaClass.static.withNewSession = { def closure ->
      Session session = [get: { Class bean, Long id -> return new CheckedClass(name: "barfoo", version: 2) }] as Session
      closure.call(session)
    }

    concurrentUpdateService.grailsApplication = [getArtefact: { handler, name ->
      return [persistentProperties: [getName: { return "name"}] as GrailsDomainClass]
    }]

    def bean = concurrentUpdateService.getPersistentValues(checked)
    assertEquals("ConcurrentUpdateService.getPersistenBean() did not return the persisted version.", "barfoo", bean.name_persisted)
  }
}
