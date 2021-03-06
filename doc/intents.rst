
Intents
=======

Simpletask supports a couple of intents which can be used by other
applications (e.g. tasker) to create tasks or display lists.

Create task in background
-------------------------

To create a task in the background, so without showing simpletask, you
can use the intent:

-  Intent action: ``nl.mpcjanssen.simpletask.BACKGROUND_TASK``

-  Intent string extra: ``task``

the intent will have one extra string ``task`` which contains the task
to be added.

For example to create a task from tasker use the following action:

-  Action: ``nl.mpcjanssen.simpletask.BACKGROUND_TASK``

-  Cat: Default

-  Mime Type: text/\*

-  Extra: task: ``<Task text with possible variables here> +tasker``

-  Target: Activity

I like to add the ``+tasker`` tag to be able to quickly filter tasks
that were created by tasker.

Open with specific filter
-------------------------

To open Simpletask with a specific filter you can use the intent:

-  Intent action: ``nl.mpcjanssen.simpletask.START_WITH_FILTER``

-  Intent extras: The following extras can be added as part of the
   intent. Note that currently the names still reflect the original
   naming of lists/tags.



+---------------+---------+------------------------------------------------------+
| Name          | Type    | Description                                          |
+===============+=========+======================================================+
| CONTEXTS      | String  | list of lists in filter separated by '\n' or ','     |
+---------------+---------+------------------------------------------------------+
| PROJECTS      | String  | list of tags in filter separated by '\n' or ','      |
+---------------+---------+------------------------------------------------------+
| PRIORITIES    | String  | list of priorities in filter separated by '\n' or ', |
+---------------+---------+------------------------------------------------------+
| CONTEXTSnot   | Boolean | true to invert the lists filter                      |
+---------------+---------+------------------------------------------------------+
| PROJECTSnot   | Boolean | true to invert the tags filter                       |
+---------------+---------+------------------------------------------------------+
| PRIORITIESnot | Boolean | true to invert the priorities filter                 |
+---------------+---------+------------------------------------------------------+
| HIDECOMPLETED | Boolean | true to hide completed tasks                         |
+---------------+---------+------------------------------------------------------+
| HIDEFUTURE    | Boolean | true to hide tasks with a threshold date             |
+---------------+---------+------------------------------------------------------+
| SORTS         | String  | active sort (see below)                              |
+---------------+---------+------------------------------------------------------+

Sorts extra
~~~~~~~~~~~

SORTS contains a comma or '' separated list of sort keys and their
direction with a ``!`` in between. Giving ``<direction>!<sort key>``.

Direction
^^^^^^^^^

-  ``+`` : Ascending

-  ``-`` : Descending

Sort keys
^^^^^^^^^

See list in
http://mpcjanssen.nl/fossil/simpletask/artifact/ac6b9bf579b8d1a9c23083031852a0fdd81efb75?ln=42-51

Example
^^^^^^^

-  The sort ``+!completed,+!alphabetical`` sorts completed tasks last
   and then sorts alphabetical.

-  The sort ``+!completed,-!alphabetical`` sorts completed tasks last
   and then sorts reversed alphabetical.

Tasker example
~~~~~~~~~~~~~~

-  Action: ``nl.mpcjanssen.simpletask.START_WITH_FILTER``

-  Cat: ``Default``

-  Mime Type:

-  Extra: ``CONTEXTS:Office,Online``

-  Extra: ``SORTS:+!completed,+!alphabetical``

-  Target: ``Activity``

Due to limitations in Tasker you can only add 2 extras. So instead you
can use the am shell command. For example:

::

    am start -a nl.mpcjanssen.simpletask.START_WITH_FILTER -e SORTS +!completed,+!alphabetical -e PROJECTS project1,project2 -e CONTEXTS @errands,@computer --ez CONTEXTSnot true -c android.intent.category.DEFAULT -S

The ``-S`` at the end will ensure the app is properly restarted if it's
already visible. However with tasker the ``-S`` seems not to work. So
there try it without.
