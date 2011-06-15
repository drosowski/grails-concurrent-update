package com.rosowski.grails.plugin

import org.hibernate.Session

class ConcurrentUpdateTagLib {

  static namespace = "conup"

  def storedValue = { attrs, body ->
    def field = attrs.field
    def bean = attrs.bean

    if (field && bean) {
      bean.withNewSession { Session session ->
        if (qualifiesForConcurrentUpdate(bean)) {
          def storedBean = session.get(bean.class, bean.id?.toLong())

          // fetch persistent value
          def value = storedBean."${field}"
          if (value) {
            out << "<div class='storedValue'>"
            out << value.toString()
            out << " <a href='#' onclick=\"document.getElementById('${field}').value = '${value.toString()}';\">"
            out << "(" + g.message(code: "stored_value.transfer", default: "transfer") + ")"
            out << "</div>"
          }
        }
      }
    }
  }

  /**
   * Checks wether the class is annotated with @ConcurrentUpdate and if the class has already been persisted.
   * Additionally the dynamic property <code>isLocked</code> is checked, which is being set by the custom validator.
   * @param bean
   * @return
   */
  private boolean qualifiesForConcurrentUpdate(def bean) {
    Class beanClass = bean.class
    boolean qualifies = (beanClass.getAnnotation(ConcurrentUpdate.class) != null)
    qualifies = qualifies & (bean.id != null)

    if (qualifies) {
      return bean.isLocked
    }
    return false
  }
}
