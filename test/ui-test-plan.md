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

## Test case: add, mark, unmark, and list a task

- Aim: A task can be added, marked done, unmarked, and shown with its current status.
- Run command: `java -cp out/production/ip SevenSix`

### Inputs

```text
write report
mark 1
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
added: write report
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [X] write report
____________________________________________________________
____________________________________________________________
1.[X] write report
____________________________________________________________
____________________________________________________________
OK, I've marked this task as not done yet:
  [ ] write report
____________________________________________________________
____________________________________________________________
1.[ ] write report
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```
