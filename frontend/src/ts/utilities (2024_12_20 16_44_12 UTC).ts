import moment from "moment";

/**
 * Functions that are not specific to ToDoView or AssigneeView
 */


/**
 * Parses a timestamp to a date string in the format "DD.MM.YYYY, hh:mm a"
 * @param timestamp
 * @returns date string
 */
export function parseTimestampToDate(timestamp: string) {
    if (timestamp == "") {
        return ""
    }
    const date = moment(timestamp, 'x')
    return date.format("DD.MM.YYYY HH:mm")
}

/**
 * Parses a timestamp to a date string in the format "DD.MM.YYYY"
 * @param timestamp
 */

export function toDateTimeLocal(timestamp: string) {
    if (timestamp == "") {
        return ""
    } else {
        const date = moment(timestamp, 'x')
        return date.format("YYYY-MM-DDTHH:mm")
    }
}

/**
 * Checks if a given timestamp is in the past.
 * @param timestamp - the timestamp to check
 */
export function dateIsInPast(timestamp: string) {
    const date = moment(timestamp, 'x')
    return date.isBefore(Date.now())
}