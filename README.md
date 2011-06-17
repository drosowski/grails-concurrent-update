Quick Summary
--------
This plugin adds concurrent update functionality to grails domain classes by annotating a class with @ConcurrentUpdate.
By annotating the class, it gets a custom validator for the version field, which is being compared to the persistent
 value. If the persistent value is greater, this means that another user has modified the object in the meantime.

Additionally the plugin provides a taglib to recover the other users changes.

    <conup:storedValue bean="${yourbean}" field="somefield"/>

Introduction
--------
The concurrent-update plugin provides functionality for the case of concurrently updating a business object by two
different users. The scenario is as follows.

Lets assume a domain class Announcement which contains one property message.

    class Announcement {

        String message

        static constraints = {
            message(blank: false)
        }
    }

When scaffolding the controller code for this domain class, we get the optimistic lock code in the update action.
Personally, I think, the code to check for optimistic locking belongs to the validation of the domain class.

Annotating a domain class
--------
When annotating a domain class with @ConcurrentUpdate, the plugin attaches a custom validator to the domain class
which checks the version.

    import com.rosowski.grails.plugin

    @ConcurrentUpdate
    class Announcement {
        ...
    }

So what happens if a user A is loading an object and while happily editing his/her object, another user B has saved
his/her changes to the object? The custom validator provided by the annotation checks if the persistent version of the
object is greater than the version stored in the hidden field. If so, the validator returns the error code
optimistic.locking.failure. For the version field to automatically be part of the autobinding process, we needed to
provide a BindEventListener.

Retrieving the other users changes
--------
The plugin goes one step further and provides a taglib with which the changes from user B can be applied to the object
by the editing user A. Lets see how we can add the tag to a typical scaffolded view.

    <conup:storedValue bean="${somebean}" field="somefield"/>

So if user A hits 'save' and another version has already been saved by user B, user A gets a warning and the ability
to apply the changes from user B to the object (for each field where the tag has been used).

GOTCHAS
--------
- Make sure you have erased the version check in the controller code, otherwise the custom validator won't be hit.
- Don't flush the save in the controller, since this leads to a duplicate validation of the object.

TODO
--------
- handle date fields