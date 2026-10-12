<script setup lang="ts">
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import config from "@/config";
import {showToast, Toast} from "@/ts/toasts";
import {faCheck, faTriangleExclamation, faXmark} from "@fortawesome/free-solid-svg-icons";
import {onMounted, type Ref, ref} from "vue";
import moment from "moment";
import {parseTimestampToDate, toDateTimeLocal, dateIsInPast} from "@/ts/utilities";
import {getCheckedToDos, sortTable, filterTable, getCheckedAssignees, emptyCreateModal} from "@/todos/todoViewPageLogic"
import {type ToDo, toDoIsValid, createRequestBody} from "@/todos/todoModel"
import type {Assignee} from "@/assignees/assigneeModel"

let toDo: Ref<ToDo> = ref({
  id: 0,
  title: "",
  description: "",
  finished: false,
  assigneeList: [],
  assigneeIdList: [] as number[],
  createdDate: 0,
  dueDate: "",
  finishedDate: 0,
  category : ""
});

const assignees: Ref<Assignee[]> = ref([]);
const toDos: Ref<ToDo[]> = ref([]);
const openToDos: Ref<ToDo[]> = ref([]);
const finishedToDos: Ref<ToDo[]> = ref([]);
// greatest id number in use
let greatestId = ref(0);
let checkedOpenToDos: Ref<boolean> = ref(false)
let checkedFinishedToDos: Ref<boolean> = ref(false)


/**
 * Fetches all ToDos from the API and assigns them to the todos variable and the openToDos/finishedToDos variable.
 * Also sets the assigneeIdList of each ToDo to the ids of its assignees and sets the greatestId variable to the greatest id in use.
 */
function fetchAllToDos() {
  fetch(`${config.apiBaseUrl}/todos`)
      .then(response => response.json())
      .then(data => data as ToDo[])
      .then(data => {
        console.log(data);
        toDos.value = data;
      })
      .then(() => {
        for (let i = 0; i < toDos.value.length; i++) {
          let toDo = toDos.value[i];
          if (toDo.dueDate == null) {
            toDo.dueDate = ""
          }
          // Set greatestId to the greatest id in use
          if (toDo.id > greatestId.value) {
            greatestId.value = toDo.id;
          }
          // Set assigneeIdList to the ids of the assignees
          let assigneeIds = []
          let toDoAssignees = toDo.assigneeList;
          for (let i = 0; i < toDoAssignees.length; i++) {
            assigneeIds.push(toDoAssignees[i].id);
          }
          toDo.assigneeIdList = assigneeIds;
          // Puts ToDos in the correct table source
          if (!toDo.finished) {
            openToDos.value.push(toDo)
          } else {
            finishedToDos.value.push(toDo)
          }
        }
      })
      .catch(error => showToast(new Toast("Error", error, "error", faXmark, 10)));
}

/**
 * Fetches all Assignees from the API and assigns them to the assignees variable.
 */
function fetchAllAssignees() {
  fetch(`${config.apiBaseUrl}/assignees`)
      .then(response => response.json())
      .then(data => data as Assignee[])
      .then(data => {
        console.log(data);
        assignees.value = data;
      })
      .catch(error => showToast(new Toast("Error", error, "error", faXmark, 10)));
}

/**
 * Deletes a specific ToDo from the API and removes it from the table.
 *
 * @param id
 */
function deleteToDo(id: number) {
  fetch(`${config.apiBaseUrl}/todos/${id}`, {method: "DELETE"})
      .then(() => {
        finishedToDos.value = finishedToDos.value.filter((t) => t.id != id);
        openToDos.value = openToDos.value.filter((t) => t.id != id);
        toDos.value = toDos.value.filter((t) => t.id != id);
      })
      .then(() => {
        showToast(new Toast("Success!", `successfully deleted ToDo with ID ${id}!`, "success", faCheck, 5))
      })
      .catch(error => showToast(new Toast("Error", error, "error", faXmark, 10)));
}

/**
 * Marks a specific ToDo as finished/unfinished in the API and moves it to the openToDos/finishedToDos variable.
 * @param id - the id of the ToDo to finish/unfinish
 * @param finished - true if the ToDo should be marked as done, false if it should be marked as open
 */
function setFinished(id: number, finished : boolean){

  fetch(`${config.apiBaseUrl}/todos/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      finished: finished,
    })
  })
      .then(() => {
        let updatedToDo = findToDoById(id);
        if(finished){
          openToDos.value = openToDos.value.filter((t) => t.id != id);
          updatedToDo.finished = true;
          updatedToDo.finishedDate = Date.now();
          finishedToDos.value.push(updatedToDo);
        } else {
          finishedToDos.value = finishedToDos.value.filter((t) => t.id != id);
          updatedToDo.finished = false;
          updatedToDo.finishedDate = 0;
          openToDos.value.push(updatedToDo);
        }
      })
      .then(() => {
        if(finished){
          showToast(new Toast("Success!", `successfully marked ToDo with ID ${id} as done!`, "success", faCheck, 5));
        } else {
          showToast(new Toast("Success!", `successfully marked ToDo with ID ${id} as open!`, "success", faCheck, 5));

        }
      })
      .catch(error => showToast(new Toast("Error", error, "error", faXmark, 10)));
}

/**
 * Updates a ToDo on the API and on the local variable.
 * Uses Data from the editToDo modal.
 * @param id - the id of the ToDo to update
 */

function editToDo(id: number) {
  const editedToDo = findToDoById(id);
  toDo.value.title = (document.getElementById("editTitle" + id.toString()) as HTMLInputElement).value;
  toDo.value.description = (document.getElementById("editDescription" + id.toString()) as HTMLInputElement).value;
  toDo.value.dueDate = (document.getElementById("editDueDate" + id.toString()) as HTMLInputElement).value;
  toDo.value.createdDate = editedToDo.createdDate;

  if (!toDoIsValid(toDo.value)) {
    return
  }
  if(toDo.value.dueDate.length > 0){
    toDo.value.dueDate = moment(toDo.value.dueDate, 'YYYY-MM-DDTHH:mm').format("x").toString();
  } else {
    toDo.value.dueDate = ""
  }
  const checkedAssignees = getCheckedAssignees(id + 'edit', assignees.value);
  toDo.value.assigneeIdList = checkedAssignees

  let fetchedToDo: ToDo;
  fetch(`${config.apiBaseUrl}/todos/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(createRequestBody(toDo.value))
  })
      .then(response => response.json())
      .then(data => data as ToDo)
      .then(data => {
        fetchedToDo = data;
      })
      .then(() => {
        if(fetchedToDo.dueDate == null) {
          fetchedToDo.dueDate = ""
        }
        editedToDo.assigneeList = [];
        editedToDo.title = fetchedToDo.title;
        editedToDo.description = fetchedToDo.description;
        editedToDo.assigneeIdList = checkedAssignees;
        editedToDo.dueDate = fetchedToDo.dueDate;
        editedToDo.category = fetchedToDo.category;
        for (let assignee of assignees.value) {
          if (checkedAssignees.includes(assignee.id)) {
            editedToDo.assigneeList.push(assignee);
          }
        }
        openToDos.value.filter((t) => t.id == id)[0] = editedToDo;
        toDos.value.filter((t) => t.id == id)[0] = editedToDo;
        showToast(new Toast("Success!", `successfully updated ToDo with ID ${id}!`, "success", faCheck, 5));
      })
      .catch(error => showToast(new Toast("Error", error, "error", faXmark, 10)));
}

/**
 * Finds a ToDo by its id.
 * @param id - the id of the ToDo
 */
function findToDoById(id: number) {
  return toDos.value.filter((t) => t.id == id)[0]
}

/**
 * Creates a new ToDo on the API and adds it to the local variable.
 * Uses Data from the createToDo modal.
 */
function createToDo() {
  toDo.value.title = (document.getElementById("createTitle") as HTMLInputElement).value;
  toDo.value.description = (document.getElementById("createDescription") as HTMLInputElement).value;
  toDo.value.assigneeIdList = getCheckedAssignees('create', assignees.value);
  toDo.value.dueDate = (document.getElementById("createDueDate") as HTMLInputElement).value;
  toDo.value.createdDate = moment((document.getElementById("createCreatedDate") as HTMLInputElement).value, 'DD.MM.YYYY HH:mm').toDate().getTime()

  if (!toDoIsValid(toDo.value)) {
    return
  }
  if(toDo.value.dueDate.length > 0){
    toDo.value.dueDate = moment(toDo.value.dueDate, 'YYYY-MM-DDTHH:mm').format("x").toString();
  } else {
    toDo.value.dueDate = ""
  }

  let fetchedToDo: ToDo;
  fetch(`${config.apiBaseUrl}/todos`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(createRequestBody(toDo.value))
  })
      .then(response => response.json())
      .then(data => data as ToDo)
      .then(data => {
        fetchedToDo = data;
      })
      .then(() => {
        showToast(new Toast("Success!", `successfully created new ToDo!`, "success", faCheck, 5));
        if (fetchedToDo.dueDate == null) {
          fetchedToDo.dueDate = ""
        }
        fetchedToDo.assigneeIdList = toDo.value.assigneeIdList;
        toDos.value.push(fetchedToDo);
        openToDos.value.push(fetchedToDo);
        greatestId.value = fetchedToDo.id;
      })
      .catch(error => showToast(new Toast("Error", error, "error", faTriangleExclamation, 10)));
}

/**
 * Empties the toDo variable.
 */
function emptyToDo() {
  toDo.value.id = 0;
  toDo.value.title = "";
  toDo.value.description = "";
  toDo.value.finished = false;
  toDo.value.assigneeList = [];
  toDo.value.assigneeIdList = [];
  toDo.value.createdDate = 0;
  toDo.value.dueDate = "";
  toDo.value.finishedDate = 0;
}

function sortToDos(field : string, order : string, table : string){
  if(table == "toDo"){
    openToDos.value = sortTable(field, order, openToDos.value)
  } else {
    finishedToDos.value = sortTable(field, order, finishedToDos.value)
  }
}

/**
 * Returns the time spent on a ToDo in a human readable format.
 * @param id
 */
function getTimeSpent(id: number) {
  let toDo = toDos.value.filter((t) => t.id == id)[0]
  let finishedDate = toDo.finishedDate
  let createdDate = toDo.createdDate
  return moment.duration(finishedDate - createdDate, "milliseconds").humanize()


}

/**
 * Deletes all selected ToDos with the given prefix from the toDoOpen or toDoFinished table.
 * @param prefix either "openCheck" or "finishedCheck"
 */

function deleteSelectedToDos(prefix: string) {
  let sourceToDos;
  if(prefix == "openCheck"){
    sourceToDos = openToDos.value
  } else {
    sourceToDos = finishedToDos.value
  }
  let toDosToDelete = getCheckedToDos(prefix, sourceToDos)
  for (let toDo of toDosToDelete) {
    deleteToDo(toDo.id)
  }
}

/**
 * Marks all selected ToDos in the toDoOpen table as done.
 */
function finishSelectedToDos() {
  for (let toDo of getCheckedToDos("openCheck", openToDos.value)) {
    setFinished(toDo.id, true)
  }
}

/**
 * Marks all selected ToDos in the toDoOpen table as open.
 */
function unfinishSelectedToDos() {
  for (let toDo of getCheckedToDos("finishedCheck", finishedToDos.value)) {
    setFinished(toDo.id, false)
  }
}

/**
 * Switches between selecting all and deselecting all ToDos in the toDoOpen or toDoFinished table.
 * @param prefix either "openCheck" or "finishedCheck"
 */
function toggleSelection(prefix: string) {
  let source;
  let allChecked;
  if (prefix == "openCheck") {
    source = openToDos.value
    allChecked = checkedOpenToDos.value
  } else {
    source = finishedToDos.value
    allChecked = checkedFinishedToDos.value
  }
  if (allChecked) {
    for (let toDo of source) {
      let checkbox = document.getElementById(prefix + toDo.id.toString()) as HTMLInputElement
      checkbox.checked = false;
    }
  } else {
    for (let toDo of source) {
      let checkbox = document.getElementById(prefix + toDo.id.toString()) as HTMLInputElement
      checkbox.checked = true;
    }
  }
  if (prefix == "openCheck") {
    checkedOpenToDos.value = !checkedOpenToDos.value
  } else {
    checkedFinishedToDos.value = !checkedFinishedToDos.value
  }
}

// on page load
onMounted(() => fetchAllToDos());
onMounted(() => fetchAllAssignees());

</script>

<template>
<div class="container-fluid">
    <div class="row py-3"></div>
    <!-- Edit Modal -->
    <div v-for="toDo in openToDos" :key="toDo.id">
      <div class="modal" tabindex="-1" v-bind:id="'modal'+toDo.id.toString()">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Edit ToDo {{ toDo.id }}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form class="modal-body" id="20">
              <div class="mb-3">
                <label class="form-label">ID</label>
                <input class="form-control" type="number" disabled v-bind:placeholder="toDo.id.toString()">
              </div>
              <div class="mb-3">
                <label class="form-label">Title</label>
                <input class="form-control" type="text" v-bind:value="toDo.title" v-bind:id="'editTitle'+toDo.id">
              </div>
              <div class="mb-3">
                <label class="form-label">Created</label>
                <input class="form-control" type="text" disabled
                       v-bind:placeholder="parseTimestampToDate(toDo.createdDate.toString())">
              </div>
              <div class="mb-3">
                <label class="form-label">Due (Optional)</label>
                <input class="form-control" type="datetime-local" v-bind:id="'editDueDate'+toDo.id"
                       v-bind:value="toDateTimeLocal(toDo.dueDate)">
              </div>
              <div class="mb-3">
                <label class="form-label">Description (Optional)</label>
                <textarea class="form-control" type="text" v-bind:value="toDo.description"
                          v-bind:id="'editDescription'+toDo.id" rows="3"></textarea>
              </div>
              <div class="mb-3">
                <label class="form-label">Category</label>
                <input class="form-control" type="text" disabled
                       v-bind:placeholder="toDo.category">
              </div>
              <p>Assignees (Optional)</p>
              <div v-for="assignee in assignees" :key="assignee.id">
                <div class="form-check" v-if="toDo.assigneeIdList.includes(assignee.id)">
                  <input class="form-check-input" type="checkbox" value=""
                         v-bind:id="toDo.id.toString()+'edit'+assignee.id.toString()"
                         checked>
                  <label class="form-check-label">
                    {{ assignee.prename }} {{ assignee.name }}
                  </label>
                </div>
                <div class="form-check" v-else>
                  <input class="form-check-input" type="checkbox" value=""
                         v-bind:id="toDo.id.toString()+'edit'+assignee.id.toString()">
                  <label class="form-check-label">
                    {{ assignee.prename }} {{ assignee.name }}
                  </label>
                </div>
              </div>
            </form>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
              <button type="button" class="btn btn-primary" @click="editToDo(toDo.id)">Save changes</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- Create Modal -->
    <div class="modal" tabindex="-1" id="createToDo">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Create new ToDo</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <form class="modal-body" id="21">
            <div class="mb-3">
              <label class="form-label">ID</label>
              <input class="form-control" type="number" disabled v-bind:placeholder="(greatestId+1).toString()">
            </div>
            <div class="mb-3">
              <label class="form-label">Title</label>
              <input class="form-control" type="text" id="createTitle" v-bind:value="toDo.title">
            </div>
            <div class="mb-3">
              <label class="form-label">Created</label>
              <input class="form-control" type="text" disabled id="createCreatedDate"
                     v-bind:value="parseTimestampToDate(Date.now().toString())">
            </div>
            <div class="mb-3">
              <label class="form-label">Due (Optional)</label>
              <input class="form-control" type="datetime-local" id="createDueDate"
                     v-bind:value="toDateTimeLocal(toDo.dueDate)">
            </div>
            <div class="mb-3">
              <label class="form-label">Description (Optional)</label>
              <textarea class="form-control" type="text" id="createDescription" v-bind:value="toDo.description"
                        rows="3"></textarea>
            </div>
            <div class="mb-3">
              <label class="form-label">Category</label>
              <input class="form-control" type="text" disabled
                    placeholder="to be identified ...">
            </div>
            <p>Assignees (Optional)</p>
            <div v-for="assignee in assignees" :key="assignee.id">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" value=""
                       v-bind:id="'create'+assignee.id.toString()">
                <label class="form-check-label">
                  {{ assignee.prename }} {{ assignee.name }}
                </label>
              </div>
            </div>
          </form>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            <button type="button" class="btn btn-primary" @click="createToDo">Save</button>
          </div>
        </div>
      </div>
    </div>
    <div class="alert alert-danger" v-if="toDos.length === 0" role="alert">
      No ToDos available on the server...
    </div>
    <div v-else>
    </div>
    <!-- Open ToDos -->
    <div class="card mx-auto">
      <p class="card-title mx-3 my-3" style="font-size: xx-large">
        Open ToDo's
      </p>
      <div class="card-body">
        <!-- Open ToDos Table -->
        <table class="table table-hover" id="toDoTable" style="text-align: left; width: 100%">
          <thead>
          <tr>
            <th scope="col" style="width: 10%">
              <form class="d-flex" role="search" id="23">
                <button v-if="!checkedOpenToDos" type="button" class="btn btn-primary"
                        @click="toggleSelection('openCheck')">Select all
                </button>
                <button v-else type="button" class="btn btn-primary" @click="toggleSelection('openCheck')">Deselect
                  all
                </button>
              </form>
              <p></p>
              <form class="d-flex" id="99">
                <input class="form-control me-2" placeholder="ID" disabled>
                <div class="btn-group" role="group">
                  <button type="button" class="btn btn-primary btn-sm" @click="sortToDos('id', 'asc', 'toDo')">
                    <font-awesome-icon style="" icon="arrow-up"></font-awesome-icon>
                  </button>
                  <button type="button" class="btn btn-primary btn-sm" @click="sortToDos('id', 'desc', 'toDo')">
                    <font-awesome-icon icon="arrow-down"></font-awesome-icon>
                  </button>
                </div>
              </form>
            </th>
            <th scope="col" style="width: 15%">
              <form class="d-flex" role="search" id="23">
                <input class="form-control me-2" type="search" placeholder="Search ..." aria-label="Search"
                       id="toDoSearchTitle" @input="filterTable('toDo')">
              </form>
              <p></p>
              <form class="d-flex" id="24">
                <input class="form-control me-2" placeholder="Title" disabled>
                <div class="btn-group" role="group">
                  <button type="button" class="btn btn-primary btn-sm" @click="sortToDos('title', 'asc', 'toDo')">
                    <font-awesome-icon icon="arrow-up"></font-awesome-icon>
                  </button>
                  <button type="button" class="btn btn-primary btn-sm" @click="sortToDos('title', 'desc', 'toDo')">
                    <font-awesome-icon icon="arrow-down"></font-awesome-icon>
                  </button>
                </div>
              </form>
            </th>
            <th scope="col" style="width: 20%">
              <form class="d-flex" role="search" id="25">
                <input class="form-control me-2" type="search" placeholder="Search ..." aria-label="Search"
                       id="toDoSearchDescription" @input="filterTable('toDo')">
              </form>
              <p></p>
              <div class="d-flex" id="26">
                <label></label>
                <input class="form-control me-2" placeholder="Description" disabled>
                <div class="btn-group" role="group">

                  <button type="button" class="btn btn-primary btn-sm" @click="sortToDos('description', 'asc', 'toDo')">
                    <font-awesome-icon icon="arrow-up"></font-awesome-icon>
                  </button>
                  <button type="button" class="btn btn-primary btn-sm"
                          @click="sortToDos('description', 'desc', 'toDo')">
                    <font-awesome-icon icon="arrow-down"></font-awesome-icon>
                  </button>
                </div>
              </div>
            </th>
            <th scope="col" style="width: 10%">
              <form class="d-flex" id="27">
                <input class="form-control me-2" placeholder="Category" disabled>
              </form>
            </th>
            <th scope="col" style="width: 15%">
              <form class="d-flex" id="27">
                <input class="form-control me-2" placeholder="Assigned to" disabled>
              </form>
            </th>
            <th scope="col" style="width: 10%">
              <form class="d-flex" id="29">
                <input class="form-control me-2" placeholder="Created" disabled>
                <div class="btn-group" role="group">
                  <button type="button" class="btn btn-primary btn-sm" @click="sortToDos('created', 'asc', 'toDo')">
                    <font-awesome-icon icon="arrow-up"></font-awesome-icon>
                  </button>
                  <button type="button" class="btn btn-primary btn-sm" @click="sortToDos('created', 'desc', 'toDo')">
                    <font-awesome-icon icon="arrow-down"></font-awesome-icon>
                  </button>
                </div>
              </form>
            </th>
            <th scope="col" style="width: 10%">
              <form class="d-flex" id="30">
                <input class="form-control me-2" placeholder="Due" disabled>
                <div class="btn-group" role="group">
                  <button type="button" class="btn btn-primary btn-sm" @click="sortToDos('due', 'asc', 'toDo')">
                    <font-awesome-icon icon="arrow-up"></font-awesome-icon>
                  </button>
                  <button type="button" class="btn btn-primary btn-sm" @click="sortToDos('due', 'desc', 'toDo')">
                    <font-awesome-icon icon="arrow-down"></font-awesome-icon>
                  </button>
                </div>
              </form>
            </th>
            <th scope="col" style="width: 10%"></th>
          </tr>
          </thead>
          <tbody v-for="toDo in openToDos" :key="toDo.id">
          <tr>
            <td style="text-align: center" id="tableId">
              <input class="form-check-input" type="checkbox" value="" v-bind:id="'openCheck'+toDo.id.toString()">
              &nbsp;&nbsp;&nbsp;&nbsp;{{ toDo.id }}
            </td>
            <td id="tableTitle">{{ toDo.title }}</td>
            <td id="tableDescription">{{ toDo.description }}</td>
            <td id="tableCategory" style="text-align: center">{{ toDo.category }}</td>
            <td id="tableAssignees">
              <ul style="list-style-type: disc">
                <li v-for="assignee in toDo.assigneeList" :key="assignee.id">
                  {{ assignee.prename }} {{ assignee.name }}
                </li>
              </ul>
            </td>
            <td id="tableCreate">
              {{ parseTimestampToDate(toDo.createdDate.toString()) }}
            </td>
            <td id="tableDue" v-if="dateIsInPast(toDo.dueDate)" style="background-color: #bb4d4d">
              {{ parseTimestampToDate(toDo.dueDate.toString()) }}
            </td>
            <td id="tableDue" v-else>{{ parseTimestampToDate(toDo.dueDate.toString()) }}</td>
            <td>
              <div class="btn-group" role="group">
                <button type="button" class="btn btn-success" @click="setFinished(toDo.id, true)"
                        style="--bs-btn-padding-y: .15rem; --bs-btn-padding-x: .35rem; --bs-btn-font-size: .75rem;">
                  <FontAwesomeIcon icon="check"/>
                  Finish
                </button>
                <button type="button" class="btn btn-secondary" data-bs-toggle="modal" @click="emptyToDo"
                        v-bind:data-bs-target="'#modal'+toDo.id.toString()"
                        style="--bs-btn-padding-y: .15rem; --bs-btn-padding-x: .75rem; --bs-btn-font-size: .75rem;">
                  <FontAwesomeIcon icon="pencil"/>
                  Edit
                </button>
                <button type="button" class="btn btn-danger" @click="deleteToDo(toDo.id)"
                        style="--bs-btn-padding-y: .15rem; --bs-btn-padding-x: .30rem;--bs-btn-font-size: .75rem;">
                  <FontAwesomeIcon icon="trash"/>
                  Delete
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
      <div class="card-footer">
        <button type="button" class="btn btn-primary offset" data-bs-toggle="modal" data-bs-target="#createToDo"
                @click="emptyCreateModal(assignees)">
          <FontAwesomeIcon icon="plus"/>
          Create new ToDo
        </button>
        <div class="btn-group offset-8" role="group">
        <button type="button" class="btn btn-success" @click="finishSelectedToDos()">
          <FontAwesomeIcon icon="check"/>
          Finish Selected
        </button>
        <button type="button" class="btn btn-danger" @click="deleteSelectedToDos('openCheck')">
          <FontAwesomeIcon icon="trash"/>
          Delete Selected
        </button>
        &nbsp;&nbsp;</div>
      </div>
    </div>
    <div class="row py-2"></div>
    <!-- Finished ToDos Accordion -->
    <div class="accordion" id="accordion">
      <div class="accordion-item">
        <div class="accordion-header">
          <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                  data-bs-target="#collapseOne" aria-expanded="false" aria-controls="collapseOne"
                  style="font-size: xx-large;">
            Finished ToDo's
          </button>
        </div>
        <div id="collapseOne" class="accordion-collapse collapse" data-bs-parent="#accordion">
          <div class="accordion-body">
            <!-- Finished ToDos Table -->
            <table class="table table-hover" id="finishedTable" style="text-align: left">
              <thead>
              <tr>
                <th scope="col" style="width: 10%">
                  <form class="d-flex" role="search" id="102">
                    <button v-if="!checkedFinishedToDos" type="button" class="btn btn-primary"
                            @click="toggleSelection('finishedCheck')">Select all
                    </button>
                    <button v-else type="button" class="btn btn-primary" @click="toggleSelection('finishedCheck')">
                      Deselect all
                    </button>
                  </form>
                  <p></p>
                  <form class="d-flex" id="31">
                    <input class="form-control me-2" placeholder="ID" disabled>
                    <div class="btn-group" role="group">
                      <button type="button" class="btn btn-primary btn-sm"
                              @click="sortToDos('id', 'asc', 'finished')">
                        <font-awesome-icon style="" icon="arrow-up"></font-awesome-icon>
                      </button>
                      <button type="button" class="btn btn-primary btn-sm"
                              @click="sortToDos('id', 'desc', 'finished')">
                        <font-awesome-icon icon="arrow-down"></font-awesome-icon>
                      </button>
                    </div>
                  </form>
                </th>
                <th scope="col" style="width: 15%">
                  <form class="d-flex" role="search" id="32">
                    <input class="form-control me-2" type="search" placeholder="Search ..." aria-label="Search"
                           id="finishedSearchTitle"
                           @input="filterTable('finished')">
                  </form>
                  <p></p>
                  <form class="d-flex" id="33">
                    <input class="form-control me-2" placeholder="Title" disabled>
                    <div class="btn-group" role="group">
                      <button type="button" class="btn btn-primary btn-sm"
                              @click="sortToDos('title', 'asc', 'finished')">
                        <font-awesome-icon icon="arrow-up"></font-awesome-icon>
                      </button>
                      <button type="button" class="btn btn-primary btn-sm"
                              @click="sortToDos('title', 'desc', 'finished')">
                        <font-awesome-icon icon="arrow-down"></font-awesome-icon>
                      </button>
                    </div>
                  </form>
                </th>
                <th scope="col" style="width: 20%">
                  <form class="d-flex" role="search" id="34">
                    <input class="form-control me-2" type="search" placeholder="Search ..." aria-label="Search"
                           id="finishedSearchDescription" @input="filterTable('finished')">
                  </form>
                  <p></p>
                  <form class="d-flex" id="35">
                    <input class="form-control me-2" placeholder="Description" disabled>
                    <div class="btn-group" role="group">

                      <button type="button" class="btn btn-primary btn-sm"
                              @click="sortToDos('description', 'asc', 'finished')">
                        <font-awesome-icon icon="arrow-up"></font-awesome-icon>
                      </button>
                      <button type="button" class="btn btn-primary btn-sm"
                              @click="sortToDos('description', 'desc', 'finished')">
                        <font-awesome-icon icon="arrow-down"></font-awesome-icon>
                      </button>
                    </div>
                  </form>
                </th>
                <th scope="col" style="width: 10%">
                  <form class="d-flex" id="36">
                    <input class="form-control me-2" placeholder="Category" disabled>
                  </form>
                </th>
                <th scope="col" style="width: 15%">
                  <form class="d-flex" id="36">
                    <input class="form-control me-2" placeholder="Assigned to" disabled>
                  </form>
                </th>
                <th scope="col" style="width: 10%">
                  <form class="d-flex" id="37">
                    <input class="form-control me-2" placeholder="Created" disabled>
                    <div class="btn-group" role="group">
                      <button type="button" class="btn btn-primary btn-sm"
                              @click="sortToDos('created', 'asc', 'finished')">
                        <font-awesome-icon icon="arrow-up"></font-awesome-icon>
                      </button>
                      <button type="button" class="btn btn-primary btn-sm"
                              @click="sortToDos('created', 'desc', 'finished')">
                        <font-awesome-icon icon="arrow-down"></font-awesome-icon>
                      </button>
                    </div>
                  </form>
                </th>
                <th scope="col" style="width: 10%">
                  <form class="d-flex" id="38">
                    <input class="form-control me-2" placeholder="Finished" disabled>
                    <div class="btn-group" role="group">
                      <button type="button" class="btn btn-primary btn-sm"
                              @click="sortToDos('finished', 'asc', 'finished')">
                        <font-awesome-icon icon="arrow-up"></font-awesome-icon>
                      </button>
                      <button type="button" class="btn btn-primary btn-sm"
                              @click="sortToDos('finished', 'desc', 'finished')">
                        <font-awesome-icon icon="arrow-down"></font-awesome-icon>
                      </button>
                    </div>
                  </form>
                </th>
                <th scope="col" style="width: 10%">
                  <form class="d-flex" id="38">
                    <input class="form-control me-2" placeholder="Spent time" disabled>
                  </form>
                </th>
              </tr>
              </thead>
              <tbody v-for="toDo in finishedToDos" :key="toDo.id">
              <tr>
                <td style="text-align: center">
                  <input class="form-check-input" type="checkbox" value=""
                         v-bind:id="'finishedCheck'+toDo.id.toString()">
                  &nbsp;&nbsp;&nbsp;&nbsp;{{ toDo.id }}
                </td>
                <td>{{ toDo.title }}</td>
                <td>{{ toDo.description }}</td>
                <td style="text-align: center">{{ toDo.category }}</td>
                <td>
                  <ul style="list-style-type: disc">
                    <li v-for="assignee in toDo.assigneeList" :key="assignee.id">
                      {{ assignee.prename }} {{ assignee.name }}
                    </li>
                  </ul>
                </td>
                <td>{{ parseTimestampToDate(toDo.createdDate.toString()) }}</td>
                <td>{{ parseTimestampToDate(toDo.finishedDate.toString()) }}</td>
                <td>{{ getTimeSpent(toDo.id) }}</td>
              </tr>
              </tbody>
            </table>
            <div class="btn-group" role="group">
            <button type="button" class="btn btn-danger" @click="deleteSelectedToDos('finishedCheck')">
              <FontAwesomeIcon icon="trash"/>
              Delete Selected
            </button>
            <button type="button" class="btn btn-secondary" @click="unfinishSelectedToDos()">
              <FontAwesomeIcon icon="rotate-right"/>
              Unfinish Selected
            </button>
            </div>
          </div>
        </div>
      </div>
    </div>
</div>
</template>