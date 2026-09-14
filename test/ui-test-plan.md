# Console UI test plan

This plan is the source of truth for manual-style console UI checks. The `test-ui` skill runs each test case's `Run command`, sends the recorded console input, and compares standard output exactly with the recorded expected output.

## Test-case format

Use the following fields for every test case:

- `Aim` explains the user-visible behaviour being checked.
- `Run command` starts the compiled program from the repository root.
- `Inputs` lists the lines entered into the console, in order.
- `Expected output` contains the complete output, including separators and spaces.

## Test case: greeting and exit

- Aim: The program welcomes the user and exits politely when the user enters `bye`.
- Run command: `rm -f .ui-test-data/greeting.txt && java -ea -Dsevensix.data.file=.ui-test-data/greeting.txt -cp out/production/ip duke.SevenSix`

### Inputs

```text
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix, your productivity thought partner.
Which deliverables are we unlocking today?
____________________________________________________________
____________________________________________________________
Great sync. Let's touch base again soon!
____________________________________________________________
```

## Test case: add, mark, unmark, and list to-do tasks

- Aim: To-do tasks can be added, marked done, unmarked, and listed with their type and status.
- Run command: `rm -f .ui-test-data/todo.txt && java -ea -Dsevensix.data.file=.ui-test-data/todo.txt -cp out/production/ip duke.SevenSix`

### Inputs

```text
todo read book
mark 1
todo borrow book
list
unmark 1
list
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix, your productivity thought partner.
Which deliverables are we unlocking today?
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [T][ ] read book
Your pipeline now holds 1 deliverable.
____________________________________________________________
____________________________________________________________
Love to see it. This deliverable has shipped:
  [T][X] read book
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [T][ ] borrow book
Your pipeline now holds 2 deliverables.
____________________________________________________________
____________________________________________________________
1.[T][X] read book
2.[T][ ] borrow book
____________________________________________________________
____________________________________________________________
Understood. I've moved this deliverable back into the pipeline:
  [T][ ] read book
____________________________________________________________
____________________________________________________________
1.[T][ ] read book
2.[T][ ] borrow book
____________________________________________________________
____________________________________________________________
Great sync. Let's touch base again soon!
____________________________________________________________
```

## Test case: parse and list deadline dates

- Aim: Deadline tasks parse date and date-time input and display readable formatted values.
- Run command: `rm -f .ui-test-data/deadline.txt && java -ea -Dsevensix.data.file=.ui-test-data/deadline.txt -cp out/production/ip duke.SevenSix`

### Inputs

```text
deadline return book /by 2019-06-06
deadline do homework /by 2/12/2019 1800
list
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix, your productivity thought partner.
Which deliverables are we unlocking today?
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [D][ ] return book (by: Jun 06 2019)
Your pipeline now holds 1 deliverable.
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [D][ ] do homework (by: Dec 02 2019 6:00 PM)
Your pipeline now holds 2 deliverables.
____________________________________________________________
____________________________________________________________
1.[D][ ] return book (by: Jun 06 2019)
2.[D][ ] do homework (by: Dec 02 2019 6:00 PM)
____________________________________________________________
____________________________________________________________
Great sync. Let's touch base again soon!
____________________________________________________________
```

## Test case: reject incorrect input and accept trailing spaces

- Aim: Empty task descriptions, unknown commands, and invalid dates produce helpful, clearly labeled errors without adding tasks, while valid commands with trailing spaces still work.
- Run command: `rm -f .ui-test-data/invalid-input.txt && java -ea -Dsevensix.data.file=.ui-test-data/invalid-input.txt -cp out/production/ip duke.SevenSix`

### Inputs

```text
todo
blah
deadline return book /by not-a-date
event meeting /from 2019-01-01 /to not-a-date
list 
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix, your productivity thought partner.
Which deliverables are we unlocking today?
____________________________________________________________
____________________________________________________________
Flagging a blocker: a todo needs a description. Let us put some substance behind it.
____________________________________________________________
____________________________________________________________
Flagging a blocker: that one is outside my wheelhouse. My core competencies are todo, deadline, event, list, mark, unmark, delete, and find.
____________________________________________________________
____________________________________________________________
Flagging a blocker: use yyyy-MM-dd, yyyy-MM-dd HHmm, or d/M/yyyy HHmm for dates and times.
____________________________________________________________
____________________________________________________________
Flagging a blocker: use yyyy-MM-dd, yyyy-MM-dd HHmm, or d/M/yyyy HHmm for dates and times.
____________________________________________________________
____________________________________________________________
Your pipeline is empty. Nothing to action right now.
____________________________________________________________
____________________________________________________________
Great sync. Let's touch base again soon!
____________________________________________________________
```

## Test case: delete a task and renumber the list

- Aim: A task can be deleted by its one-based list number, and the remaining tasks are renumbered.
- Run command: `rm -f .ui-test-data/delete.txt && java -ea -Dsevensix.data.file=.ui-test-data/delete.txt -cp out/production/ip duke.SevenSix`

### Inputs

```text
todo read book
deadline return book /by 2019-06-06
event project meeting /from 2019-08-06 1400 /to 2019-08-06 1600
todo join sports club
list
delete 3
list
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix, your productivity thought partner.
Which deliverables are we unlocking today?
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [T][ ] read book
Your pipeline now holds 1 deliverable.
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [D][ ] return book (by: Jun 06 2019)
Your pipeline now holds 2 deliverables.
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [E][ ] project meeting (from: Aug 06 2019 2:00 PM to: Aug 06 2019 4:00 PM)
Your pipeline now holds 3 deliverables.
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [T][ ] join sports club
Your pipeline now holds 4 deliverables.
____________________________________________________________
____________________________________________________________
1.[T][ ] read book
2.[D][ ] return book (by: Jun 06 2019)
3.[E][ ] project meeting (from: Aug 06 2019 2:00 PM to: Aug 06 2019 4:00 PM)
4.[T][ ] join sports club
____________________________________________________________
____________________________________________________________
Noted. I've descoped this deliverable:
  [E][ ] project meeting (from: Aug 06 2019 2:00 PM to: Aug 06 2019 4:00 PM)
Your pipeline now holds 3 deliverables.
____________________________________________________________
____________________________________________________________
1.[T][ ] read book
2.[D][ ] return book (by: Jun 06 2019)
3.[T][ ] join sports club
____________________________________________________________
____________________________________________________________
Great sync. Let's touch base again soon!
____________________________________________________________
```

## Test case: parse and list event dates

- Aim: Event tasks parse date and date-time input and display readable formatted values.
- Run command: `rm -f .ui-test-data/event.txt && java -ea -Dsevensix.data.file=.ui-test-data/event.txt -cp out/production/ip duke.SevenSix`

### Inputs

```text
event project meeting /from 2019-08-06 1400 /to 2019-08-06 1600
event orientation week /from 4/10/2019 /to 11/10/2019
list
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix, your productivity thought partner.
Which deliverables are we unlocking today?
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [E][ ] project meeting (from: Aug 06 2019 2:00 PM to: Aug 06 2019 4:00 PM)
Your pipeline now holds 1 deliverable.
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [E][ ] orientation week (from: Oct 04 2019 to: Oct 11 2019)
Your pipeline now holds 2 deliverables.
____________________________________________________________
____________________________________________________________
1.[E][ ] project meeting (from: Aug 06 2019 2:00 PM to: Aug 06 2019 4:00 PM)
2.[E][ ] orientation week (from: Oct 04 2019 to: Oct 11 2019)
____________________________________________________________
____________________________________________________________
Great sync. Let's touch base again soon!
____________________________________________________________
```

## Test case: save tasks and load them on startup

- Aim: Typed deadline values are written to disk after they are added and restored when SevenSix starts again.
- Run command: `rm -f .ui-test-data/persistence.txt && printf 'deadline saved deadline /by 2/12/2019 1800\nbye\n' | java -Dsevensix.data.file=.ui-test-data/persistence.txt -cp out/production/ip duke.SevenSix > /dev/null && java -ea -Dsevensix.data.file=.ui-test-data/persistence.txt -cp out/production/ip duke.SevenSix`

### Inputs

```text
list
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix, your productivity thought partner.
Which deliverables are we unlocking today?
____________________________________________________________
____________________________________________________________
1.[D][ ] saved deadline (by: Dec 02 2019 6:00 PM)
____________________________________________________________
____________________________________________________________
Great sync. Let's touch base again soon!
____________________________________________________________
```

## Test case: ignore corrupted task records

- Aim: A malformed record does not stop SevenSix from loading valid records from the same file.
- Run command: `mkdir -p .ui-test-data && printf 'T | 1 | valid saved task\nnot a valid record\nD | 0 | return book | 2019-06-06\n' > .ui-test-data/corrupted.txt && java -ea -Dsevensix.data.file=.ui-test-data/corrupted.txt -cp out/production/ip duke.SevenSix`

### Inputs

```text
list
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix, your productivity thought partner.
Which deliverables are we unlocking today?
____________________________________________________________
____________________________________________________________
1.[T][X] valid saved task
2.[D][ ] return book (by: Jun 06 2019)
____________________________________________________________
____________________________________________________________
Great sync. Let's touch base again soon!
____________________________________________________________
```

## Test case: find tasks by keyword

- Aim: The find command returns matching task descriptions without changing the task list, regardless of keyword letter case, and reports when there are no matches.
- Run command: `rm -f .ui-test-data/find.txt && java -ea -Dsevensix.data.file=.ui-test-data/find.txt -cp out/production/ip duke.SevenSix`

### Inputs

```text
todo read book
deadline return book /by 2019-06-06
todo join sports club
find BOOK
find holiday
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix, your productivity thought partner.
Which deliverables are we unlocking today?
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [T][ ] read book
Your pipeline now holds 1 deliverable.
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [D][ ] return book (by: Jun 06 2019)
Your pipeline now holds 2 deliverables.
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [T][ ] join sports club
Your pipeline now holds 3 deliverables.
____________________________________________________________
____________________________________________________________
Here is what surfaced in your pipeline:
1.[T][ ] read book
2.[D][ ] return book (by: Jun 06 2019)
____________________________________________________________
____________________________________________________________
Nothing in your pipeline matches that search.
____________________________________________________________
____________________________________________________________
Great sync. Let's touch base again soon!
____________________________________________________________
```

## Test case: undo the most recent task change

- Aim: The undo command removes the most recently added task and reports an error when there is no task-changing command left to undo.
- Run command: `rm -f .ui-test-data/undo.txt && java -ea -Dsevensix.data.file=.ui-test-data/undo.txt -cp out/production/ip duke.SevenSix`

### Inputs

```text
todo read book
undo
list
undo
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix, your productivity thought partner.
Which deliverables are we unlocking today?
____________________________________________________________
____________________________________________________________
Circling back on your ask. I've actioned this deliverable:
  [T][ ] read book
Your pipeline now holds 1 deliverable.
____________________________________________________________
____________________________________________________________
Rolled back. Your pipeline is restored to its previous state.
____________________________________________________________
____________________________________________________________
Your pipeline is empty. Nothing to action right now.
____________________________________________________________
____________________________________________________________
Flagging a blocker: there is nothing in the rollback history yet.
____________________________________________________________
____________________________________________________________
Great sync. Let's touch base again soon!
____________________________________________________________
```
