import type {Assignee} from "@/assignees/assigneeModel"
import {showToast, Toast} from "@/ts/toasts";
import {faTriangleExclamation} from "@fortawesome/free-solid-svg-icons";
import moment from "moment/moment";

export interface ToDo {
    id: number;
    title: string;
    description: string;
    finished: boolean;
    assigneeList: Assignee[];
    assigneeIdList: number[];
    createdDate: number;
    dueDate: string;
    finishedDate: number
    category : string;
}

/**
 * Checks if the given ToDo is valid.
 * @param toDo - the ToDo to check
 */
export function toDoIsValid(toDo: ToDo) {
    let valid = true;
    if (toDo.title.length < 1) {
        showToast(new Toast("Error", "ToDo needs a title!", "error", faTriangleExclamation, 10));
        valid = false;
    }
    if (toDo.dueDate.length > 0) {
        if (!(toDo.dueDate.match('^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$'))) {
            showToast(new Toast("Error", "Due date is not in the right format!", "error", faTriangleExclamation, 10));
            valid = false
        } else {
            const dueDate = moment(toDo.dueDate, 'YYYY-MM-DDTHH:mm')
            const createdDate = moment(toDo.createdDate, 'x')
            if (dueDate.isBefore(createdDate)) {
                showToast(new Toast("Error", "DueDate is in the past!", "error", faTriangleExclamation, 10));
                valid = false;
            }
        }
    }
    return valid;
}

/**
 * Returns a request body for a POST/PUT request to the server.
 * @param todo
 */
export function createRequestBody(todo: ToDo){
    if(todo.dueDate.length > 0){
    return {
        title: todo.title,
        description: todo.description,
        dueDate: todo.dueDate,
        assigneeIdList: todo.assigneeIdList,
    }} else {
        return {
            title: todo.title,
            description: todo.description,
            dueDate: null,
            assigneeIdList: todo.assigneeIdList,
        }
    }
}