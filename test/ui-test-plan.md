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
- Run command: `rm -f .ui-test-data/greeting.txt && java -Dsevensix.data.file=.ui-test-data/greeting.txt -cp out/production/ip SevenSix`

### Inputs

```text
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix.
What can I do for you?
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Test case: add, mark, unmark, and list to-do tasks

- Aim: To-do tasks can be added, marked done, unmarked, and listed with their type and status.
- Run command: `rm -f .ui-test-data/todo.txt && java -Dsevensix.data.file=.ui-test-data/todo.txt -cp out/production/ip SevenSix`

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
Hello! I'm SevenSix.
What can I do for you?
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [T][X] read book
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] borrow book
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[T][X] read book
2.[T][ ] borrow book
____________________________________________________________
____________________________________________________________
OK, I've marked this task as not done yet:
  [T][ ] read book
____________________________________________________________
____________________________________________________________
1.[T][ ] read book
2.[T][ ] borrow book
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Test case: parse and list deadline dates

- Aim: Deadline tasks parse date and date-time input and display readable formatted values.
- Run command: `rm -f .ui-test-data/deadline.txt && java -Dsevensix.data.file=.ui-test-data/deadline.txt -cp out/production/ip SevenSix`

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
Hello! I'm SevenSix.
What can I do for you?
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [D][ ] return book (by: Jun 06 2019)
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [D][ ] do homework (by: Dec 02 2019 6:00 PM)
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[D][ ] return book (by: Jun 06 2019)
2.[D][ ] do homework (by: Dec 02 2019 6:00 PM)
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Test case: reject incorrect input and accept trailing spaces

- Aim: Empty task descriptions, unknown commands, and invalid dates produce helpful 67-themed errors without adding tasks, while valid commands with trailing spaces still work.
- Run command: `rm -f .ui-test-data/invalid-input.txt && java -Dsevensix.data.file=.ui-test-data/invalid-input.txt -cp out/production/ip SevenSix`

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
Hello! I'm SevenSix.
What can I do for you?
____________________________________________________________
____________________________________________________________
676767!!! a todo needs a description. Give it a little something to do!
____________________________________________________________
____________________________________________________________
676767!!! I don't know that command yet. Try todo, deadline, event, list, mark, unmark, or delete.
____________________________________________________________
____________________________________________________________
676767!!! use yyyy-MM-dd, yyyy-MM-dd HHmm, or d/M/yyyy HHmm for dates and times.
____________________________________________________________
____________________________________________________________
676767!!! use yyyy-MM-dd, yyyy-MM-dd HHmm, or d/M/yyyy HHmm for dates and times.
____________________________________________________________
____________________________________________________________
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Test case: delete a task and renumber the list

- Aim: A task can be deleted by its one-based list number, and the remaining tasks are renumbered.
- Run command: `rm -f .ui-test-data/delete.txt && java -Dsevensix.data.file=.ui-test-data/delete.txt -cp out/production/ip SevenSix`

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
Hello! I'm SevenSix.
What can I do for you?
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [D][ ] return book (by: Jun 06 2019)
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [E][ ] project meeting (from: Aug 06 2019 2:00 PM to: Aug 06 2019 4:00 PM)
Now you have 3 tasks in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] join sports club
Now you have 4 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[T][ ] read book
2.[D][ ] return book (by: Jun 06 2019)
3.[E][ ] project meeting (from: Aug 06 2019 2:00 PM to: Aug 06 2019 4:00 PM)
4.[T][ ] join sports club
____________________________________________________________
____________________________________________________________
Noted. I've removed this task:
  [E][ ] project meeting (from: Aug 06 2019 2:00 PM to: Aug 06 2019 4:00 PM)
Now you have 3 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[T][ ] read book
2.[D][ ] return book (by: Jun 06 2019)
3.[T][ ] join sports club
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Test case: parse and list event dates

- Aim: Event tasks parse date and date-time input and display readable formatted values.
- Run command: `rm -f .ui-test-data/event.txt && java -Dsevensix.data.file=.ui-test-data/event.txt -cp out/production/ip SevenSix`

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
Hello! I'm SevenSix.
What can I do for you?
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [E][ ] project meeting (from: Aug 06 2019 2:00 PM to: Aug 06 2019 4:00 PM)
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [E][ ] orientation week (from: Oct 04 2019 to: Oct 11 2019)
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[E][ ] project meeting (from: Aug 06 2019 2:00 PM to: Aug 06 2019 4:00 PM)
2.[E][ ] orientation week (from: Oct 04 2019 to: Oct 11 2019)
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Test case: save tasks and load them on startup

- Aim: Typed deadline values are written to disk after they are added and restored when SevenSix starts again.
- Run command: `rm -f .ui-test-data/persistence.txt && printf 'deadline saved deadline /by 2/12/2019 1800\nbye\n' | java -Dsevensix.data.file=.ui-test-data/persistence.txt -cp out/production/ip SevenSix > /dev/null && java -Dsevensix.data.file=.ui-test-data/persistence.txt -cp out/production/ip SevenSix`

### Inputs

```text
list
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix.
What can I do for you?
____________________________________________________________
____________________________________________________________
1.[D][ ] saved deadline (by: Dec 02 2019 6:00 PM)
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Test case: ignore corrupted task records

- Aim: A malformed record does not stop SevenSix from loading valid records from the same file.
- Run command: `mkdir -p .ui-test-data && printf 'T | 1 | valid saved task\nnot a valid record\nD | 0 | return book | 2019-06-06\n' > .ui-test-data/corrupted.txt && java -Dsevensix.data.file=.ui-test-data/corrupted.txt -cp out/production/ip SevenSix`

### Inputs

```text
list
bye
```

### Expected output

```text
____________________________________________________________
Hello! I'm SevenSix.
What can I do for you?
____________________________________________________________
____________________________________________________________
1.[T][X] valid saved task
2.[D][ ] return book (by: Jun 06 2019)
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```
