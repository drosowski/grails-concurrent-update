package com.rosowski.grails.plugin

import org.hibernate.Session

class ConcurrentUpdateTagLib {

  def grailsApplication

  static namespace = "conup"

  /**
   * Retrieves the value for a given field as it is stored in the database. Can be used to show cnocurrent changes
   * i.e. from another user to the editing user.
   *
   * @attrs bean REQUIRED the bean for which the field should be retrieved
   * @attrs field REQUIRED the name of the field which should be retrieved
   * @attrs optionKey OPTIONAL the optionKey for a select box (i.e. when selecting another domain class)
   * @attrs optionValue OPTIONAL the optionValue for a select box (i.e. when selecting another domain class)
   */
  def storedValue = { attrs, body ->
    def field = attrs.field
    def bean = attrs.bean
    def optionKey = attrs.optionKey
    def optionValue = attrs.optionValue

    if (field && bean) {
      bean.withNewSession { Session session ->
        if (qualifiesForConcurrentUpdate(bean)) {
          def storedBean = session.get(bean.class, bean.id?.toLong())

          // fetch persistent value
          def value = storedBean."${field}"
          // render the stored value if it differs from the beans current value
          if (value && value != bean."${field}") {
            out << renderValue(value, field, optionKey, optionValue)
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
  private boolean qualifiesForConcurrentUpdate(bean) {
    Class beanClass = bean.class
    boolean qualifies = (beanClass.getAnnotation(ConcurrentUpdate.class) != null)
    qualifies = qualifies & (bean.id != null)

    if (qualifies) {
      return bean.isLocked
    }
    return false
  }

  private String renderValue(value, field, optionKey, optionValue) {
    StringBuilder builder = new StringBuilder()
    builder << "<div class='storedValue'>"

    if (value instanceof Date) {
      builder << g.formatDate(date: value, locale: request.locale)
    }
    else if (isGrailsDomainClass(value)) {
      builder << value."${optionValue}"
      builder << """<script type="text/javascript">
                  function getOptionForValue(optionValue) {
                    var selectBox = document.getElementById('${field}.${optionKey}');
                    if(selectBox) {
                      for(var i = 0; i < selectBox.options.length; i++) {
                        if(selectBox.options[i].value == optionValue) {
                          return selectBox.options[i];
                        }
                      }
                    }
                  }
                  </script>"""
      def keyValue = value."${optionKey}"
      builder << " <a href='#' onclick=\"var valueOption = getOptionForValue('${keyValue}'); if(valueOption) valueOption.selected = 'selected'\">"
      builder << "(" + g.message(code: "stored_value.transfer", default: "transfer") + ")"
      builder << "</a>"
    }
    else {
      builder << value.toString()
      builder << " <a href='#' onclick=\"document.getElementById('${field}').value = '${value.toString()}';\">"
      builder << "(" + g.message(code: "stored_value.transfer", default: "transfer") + ")"
      builder << "</a>"
    }

    builder << "</div>"
    return builder.toString()
  }

  boolean isGrailsDomainClass(aInstance) {
    return aInstance && grailsApplication.isDomainClass(aInstance.getClass())
  }
}
