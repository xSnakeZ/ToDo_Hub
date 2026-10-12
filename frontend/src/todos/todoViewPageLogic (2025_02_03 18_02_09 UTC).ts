import type {ToDo} from "@/todos/todoModel";
import type {Assignee} from "@/assignees/assigneeModel";



/**
 * Empties the input fields of the "createToDo" modal. This is used after a ToDo was created and the modal is closed.
 */
export function emptyCreateModal(sourceAssignees: Assignee[]) {

    const title = document.getElementById("createTitle") as HTMLInputElement;
    const description = document.getElementById("createDescription") as HTMLInputElement;
    const dueDate = document.getElementById("createDueDate") as HTMLInputElement;
    title.value = "";
    description.value = "";
    dueDate.value = "";

    for (let i = 0; i < sourceAssignees.length; i++) {
        const assigneeId = sourceAssignees[i].id;
        const checkbox = (document.getElementById("create" + assigneeId.toString()) as HTMLInputElement)
        checkbox.checked = false;
    }
}

/**
 * Returns an array containing the ids of all assignees that are checked in the modal with the given prefix.
 * @param prefix
 */
export function getCheckedAssignees(prefix: string, sourceAssignees: Assignee[]) {
    const checkedAssignees: number [] = [];
    for (let i = 0; i < sourceAssignees.length; i++) {
        const currentId = sourceAssignees[i].id
        const checkbox = document.getElementById(prefix + currentId.toString()) as HTMLInputElement
        if (checkbox.checked) {
            checkedAssignees.push(currentId)
        }
    }
    return checkedAssignees
}

/**
 * Returns an array containing all ToDos that are checked in the finishedToDos table.
 */
export function getCheckedToDos(prefix: string, sourceToDos : ToDo[]) {
    const checkedToDos: ToDo [] = [];

    for (let i = 0; i < sourceToDos.length; i++) {
        const currentToDo = sourceToDos[i]
        const checkbox = document.getElementById(prefix + currentToDo.id.toString()) as HTMLInputElement
        if (checkbox.checked) {
            checkedToDos.push(currentToDo)
        }
    }
    return checkedToDos;
}

/**
 * Filters the table with the given table by values of the "Title" and "Description" search fields.
 * @param tablePrefix either "toDo" or "finished"
 */
export function filterTable(tablePrefix: string) {

    const table = document.getElementById(tablePrefix + "Table")!;
    const titleCol = 1
    const descriptionCol = 2
    const rows = table.getElementsByTagName("tr");
    const subStringTitle = document.getElementById(tablePrefix + "SearchTitle") as HTMLInputElement;
    const subStringDescription = document.getElementById(tablePrefix + "SearchDescription") as HTMLInputElement;


    for (let i = 1; i < rows.length; i++) {

        const cells = rows[i].getElementsByTagName("td");
        const cellTitle = cells[titleCol]
        const cellDescription = cells[descriptionCol]
        let showRow = true;

        if (!(cellTitle?.textContent?.toUpperCase().includes(subStringTitle.value.toUpperCase()))) {
            showRow = false;
        }
        if (!(cellDescription?.textContent?.toUpperCase().includes(subStringDescription.value.toUpperCase()))) {
            showRow = false;
        }
        if (showRow) {
            rows[i].style.display = "";
        } else {
            rows[i].style.display = "none";
        }
    }
}

/**
 * Sorts the OpenToDos or FinishedToDos table by the given field and sequence.
 *
 * @param field - the column to sort by
 * @param sequence - the sequence to sort by (asc or desc)
 * @param table - the table to sort (open or finished)
 */

export function sortTable(field: string, sequence: string, unsortedToDos: ToDo[]) {

    switch (field) {
        case "id":
            if (sequence == "asc") {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.id > b.id) ? 1 : -1)
            } else {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.id < b.id) ? 1 : -1)
            }
            break;
        case "title":
            if (sequence == "asc") {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.title > b.title) ? 1 : -1)
            } else {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.title < b.title) ? 1 : -1)
            }
            break;
        case "description":
            if (sequence == "asc") {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.description > b.description) ? 1 : -1)
            } else {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.description < b.description) ? 1 : -1)
            }
            break;
        case "created":
            if (sequence == "asc") {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.createdDate > b.createdDate) ? 1 : -1)
            } else {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.createdDate < b.createdDate) ? 1 : -1)
            }
            break;
        case "due":
            if (sequence == "asc") {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.dueDate > b.dueDate) ? 1 : -1)
            } else {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.dueDate < b.dueDate) ? 1 : -1)
            }
            break;
        case "finished":
            if (sequence == "asc") {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.finishedDate > b.finishedDate) ? 1 : -1)
            } else {
                unsortedToDos = unsortedToDos.sort((a, b) => (a.finished < b.finished) ? 1 : -1)
            }
    }
    return unsortedToDos;
}