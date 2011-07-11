package com.rosowski.grails.plugin

import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.hibernate.Session

class ConcurrentUpdateService {

  def grailsApplication

  static transactional = false

  /**
   * Retrieves the bean as it is stored in the database. Can be used to show cnocurrent changes
   * i.e. from another user to the editing user.
   * @param bean
   */
  def getPersistentValues(bean) {
    if (qualifiesForConcurrentUpdate(bean)) {
      bean.withNewSession { Session session ->
        def storedBean = session.get(bean.class, bean.id?.toLong())
        if (storedBean) {
          bean = copyPersistentProperties(bean, storedBean)
        }
      }
    }
    return bean
  }

/**
 * Checks wether the class is annotated with @ConcurrentUpdate and if the class has already been persisted.
 * Additionally the dynamic property <code>isLocked</code> is checked, which is being set by the custom validator.
 * @param bean
 * @return
 */
  private boolean qualifiesForConcurrentUpdate(bean) {
    if (bean != null) {
      Class beanClass = bean.class
      boolean qualifies = (beanClass.getAnnotation(ConcurrentUpdate.class) != null)
      qualifies = qualifies & (bean.id != null)

      if (qualifies) {
        return bean.isLocked
      }
    }
    return false
  }

  private Object copyPersistentProperties(bean, storedBean) {
    def domainClass = grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, bean.class.getName())
    def properties = domainClass.persistentProperties

    properties.each { prop ->
      bean.metaClass."${prop.name}_persisted" = storedBean."${prop.name}"
    }
    return bean
  }
}
