package de.unistuttgart.iste.ese.api.utilites;

import de.unistuttgart.iste.ese.api.model.Assignee;
import de.unistuttgart.iste.ese.api.model.ToDo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
public class CSVHelper {

    /**
     * Converts a list of ToDos to a CSV file
     * @param toDos List of ToDos
     * @return ByteArrayInputStream that containing the CSV file
     */
    public static ByteArrayInputStream toDosToCSV(List<ToDo> toDos) {
        String[] HEADERS = {"id", "title", "description", "finished", "assignees", "createdDate", "dueDate", "finishedDate", "category"};
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader(HEADERS)
            .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), csvFormat)) {
            for (ToDo toDo : toDos) {

                String assignees = assigneesAsString(toDo);

                List<String> data = Arrays.asList(
                    String.valueOf(toDo.getId()),
                    toDo.getTitle(),
                    toDo.getDescription(),
                    String.valueOf(toDo.setFinished()),
                    assignees,
                    formatDate(toDo.getCreatedDate()),
                    formatDate(toDo.getDueDate()),
                    formatDate(toDo.getFinishedDate()),
                    toDo.getCategory()
                    );
                csvPrinter.printRecord(data);
            }
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the assignees of a ToDo as a String used for the CSV file
     * @param t ToDo
     * @return String containing the ToDo's assignees
     */
    private static String assigneesAsString(ToDo t) {
        StringBuilder assignees = new StringBuilder();
        int i;
        if (!t.getAssigneeList().isEmpty()) {
            for (i = 0; i < t.getAssigneeList().size() - 1; i++) {
                Assignee currentAssignee = t.getAssigneeList().get(i);
                assignees.append(currentAssignee.getPrename() + " " + currentAssignee.getName() + "+");
            }
            assignees.append(t.getAssigneeList().get(i).getPrename() + " " + t.getAssigneeList().get(i).getName());
        } else {
            assignees.append("");
        }
        return assignees.toString();
    }

    /**
     * Formats a date to a String used for the CSV file
     * @param date Date
     * @return String containing the formatted date
     */
    private static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
       return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
