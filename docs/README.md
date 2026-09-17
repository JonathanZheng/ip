# SevenSix User Guide

// Product screenshot goes here

// Product intro goes here

## Getting help

Enter `help` to display the command guide in the console or GUI. The guide lists
all commands, explains task numbers and date formats, and includes examples.

| Command | Purpose |
| --- | --- |
| `help` | Show the command guide. |
| `todo <description>` | Add a task without a date. |
| `deadline <description> /by <date/time>` | Add a task with a due date. |
| `event <description> /from <date/time> /to <date/time>` | Add an event. |
| `list` | Show tasks and their numbers. |
| `mark <number>` | Mark a task as done. |
| `unmark <number>` | Mark a task as not done. |
| `delete <number>` | Remove a task. |
| `find <keyword>` | Search descriptions, ignoring letter case. |
| `undo` | Undo the most recent task change, once. |
| `bye` | Exit SevenSix. |

Replace placeholders such as `<description>` with your own values; omit the
angle brackets. Task numbers come from `list` and start at 1. Dates can use
`yyyy-MM-dd` or `d/M/yyyy`, optionally followed by a time in `HHmm` or `HH:mm`.
Events must end after they start; an omitted time means midnight.

Examples shown by `help`:

```text
todo read book
deadline submit report /by 2026-09-30 1800
event team meeting /from 2026-09-18 1400 /to 2026-09-18 1500
mark 1
```

Use lowercase `help` without arguments. Surrounding spaces and tabs are accepted,
but `help todo` is rejected. Viewing help leaves tasks, saved data, and undo
history unchanged, and remains available when storage cannot be read.

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Feature ABC

// Feature details


## Feature XYZ

// Feature details

## Undoing the most recent task change

Use `undo` to reverse the most recent task-changing command during the current
session. It can undo adding, marking, unmarking, or deleting a task, including
to-dos, deadlines, and events.

Example: `undo`

The previous task list is restored and the change is saved to disk. Only one
undo is available at a time.
