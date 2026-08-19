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
- Run command: `java -cp out/production/ip SevenSix`

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
- Run command: `java -cp out/production/ip SevenSix`

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

## Test case: add and list deadline tasks

- Aim: Deadline tasks retain the due text exactly as entered and show the deadline type.
- Run command: `java -cp out/production/ip SevenSix`

### Inputs

```text
deadline return book /by Sunday
deadline do homework /by no idea :-p
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
  [D][ ] return book (by: Sunday)
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [D][ ] do homework (by: no idea :-p)
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[D][ ] return book (by: Sunday)
2.[D][ ] do homework (by: no idea :-p)
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Test case: reject incorrect input and accept trailing spaces

- Aim: Empty task descriptions and unknown commands produce helpful 67-themed errors without adding tasks, while valid commands with trailing spaces still work.
- Run command: `java -cp out/production/ip SevenSix`

### Inputs

```text
todo
blah
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
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Test case: delete a task and renumber the list

- Aim: A task can be deleted by its one-based list number, and the remaining tasks are renumbered.
- Run command: `java -cp out/production/ip SevenSix`

### Inputs

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
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
  [D][ ] return book (by: June 6th)
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
Now you have 3 tasks in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] join sports club
Now you have 4 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[T][ ] read book
2.[D][ ] return book (by: June 6th)
3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
4.[T][ ] join sports club
____________________________________________________________
____________________________________________________________
Noted. I've removed this task:
  [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
Now you have 3 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[T][ ] read book
2.[D][ ] return book (by: June 6th)
3.[T][ ] join sports club
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Test case: add and list event tasks

- Aim: Event tasks retain their start and end text exactly as entered and show the event type.
- Run command: `java -cp out/production/ip SevenSix`

### Inputs

```text
event project meeting /from Mon 2pm /to 4pm
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
  [E][ ] project meeting (from: Mon 2pm to: 4pm)
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [E][ ] orientation week (from: 4/10/2019 to: 11/10/2019)
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[E][ ] project meeting (from: Mon 2pm to: 4pm)
2.[E][ ] orientation week (from: 4/10/2019 to: 11/10/2019)
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```
