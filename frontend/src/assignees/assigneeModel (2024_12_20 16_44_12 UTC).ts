import {showToast, Toast} from "@/ts/toasts";
import {faTriangleExclamation} from "@fortawesome/free-solid-svg-icons";

export interface Assignee {
    id: number;
    prename: string;
    name: string;
    email: string;
}

/**
 * Checks if the given assignee is valid.
 * @param assignee - the assignee to check
 */
export function assigneeIsValid(assignee: Assignee) {
    let valid = true;
    if (assignee.name.length < 1 || assignee.prename.length < 1 || assignee.email.length < 1) {
        showToast(new Toast("Error", "Please fill out all fields!", "error", faTriangleExclamation, 10));
        valid = false;
    }

    if (!assignee.email.match(".*uni-stuttgart\\.de$") || !assignee.email.match("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9]")) {
        showToast(new Toast("Error", "Please enter a valid university email address!", "error", faTriangleExclamation, 10));
        valid = false
    }
    return valid;
}