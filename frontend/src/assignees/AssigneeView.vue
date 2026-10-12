<script setup lang="ts">
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import config from "@/config";
import {showToast, Toast} from "@/ts/toasts";
import {faCheck, faTriangleExclamation} from "@fortawesome/free-solid-svg-icons";
import {onMounted, type Ref, ref} from "vue";
import {type Assignee, assigneeIsValid} from "@/assignees/assigneeModel";

let assignee: Ref<Assignee> = ref({
  id: 0,
  prename: "",
  name: "",
  email: "",
});

const assignees: Ref<Assignee[]> = ref([]);
let greatestId: Ref<number> = ref(0);

/**
 * Fetches all assignees from the API and stores them in the 'assignees' variable.
 * Also initializes 'greatestId' and 'numberTodosMap'.
 */
function fetchAllAssignees() {
  fetch(`${config.apiBaseUrl}/assignees`)
      .then(response => response.json())
      .then(data => data as Assignee[])
      .then(data => {
        console.log(data);
        assignees.value = data;
      })
      .then(() => initializeGreatestId())
      .catch(error => showToast(new Toast("Error", error, "error", faTriangleExclamation, 10)));
}

/**
 * Updates an assignee on the API.
 * @param id
 */
function editAssignee(id: number) {
  assignee.value.prename = (document.getElementById('editPrename' + id.toString()) as HTMLInputElement).value;
  assignee.value.name = (document.getElementById('editName' + id.toString()) as HTMLInputElement).value;
  assignee.value.email = (document.getElementById('editEmail' + id.toString()) as HTMLInputElement).value;

  if(!assigneeIsValid(assignee.value)){
    return;
  }

  fetch(`${config.apiBaseUrl}/assignees/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(assignee.value)
  })
      .then(() => {
        fetchAllAssignees()
        showToast(new Toast("Alert", `Successfully updated assignee with ID ${id}!`, "success", faCheck, 5));
      })
      .catch(error => showToast(new Toast("Error", error, "error", faTriangleExclamation, 10)));
}

/**
 * Deletes an assignee from the API.
 * @param id
 */
function deleteAssignee(id: number) {
  fetch(`${config.apiBaseUrl}/assignees/${id}`, {method: "DELETE"})
      .then(() => {
        assignees.value = assignees.value.filter((assignee) => assignee.id !== id);
        showToast(new Toast("Alert", `Successfully deleted assignee with ID ${id}!`, "success", faCheck, 5));
      })
      .catch(error => showToast(new Toast("Error", error, "error", faTriangleExclamation, 10)));
}

/**
 * Creates a new assignee on the API.
 */
function createAssignee() {
  let prename = (document.getElementById("createPrename") as HTMLInputElement).value;
  let name = (document.getElementById("createName") as HTMLInputElement).value;
  let email = (document.getElementById("createEmail") as HTMLInputElement).value;

  if (name.length < 1 || prename.length < 1 || email.length < 1) {
    showToast(new Toast("Error", "Please fill out all fields!", "error", faTriangleExclamation, 10));
    return;
  }
  if ((!email.match(".*uni-stuttgart\\.de$")) || ((!email.match("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9]")))
  ) {
    showToast(new Toast("Error", "Please enter a valid university email address!", "error", faTriangleExclamation, 10));
    return;
  }

  assignee.value.prename = prename;
  assignee.value.name = name;
  assignee.value.email = email;


  fetch(`${config.apiBaseUrl}/assignees`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(assignee.value)
  })
      .then(() => {
        fetchAllAssignees()
        showToast(new Toast("Alert", `Successfully created new assignee!`, "success", faCheck, 5));
      })
      .catch(error => showToast(new Toast("Error", error, "error", faTriangleExclamation, 10)));
}

/**
 * Empties the 'assignee' variable.
 */
function emptyAssignee() {
  assignee.value = {
    id: 0,
    prename: "",
    name: "",
    email: "",
  };
}
/**
 * Initializes the 'greatestId' variable.
 */
function initializeGreatestId() {
  for (let i = 0; i < assignees.value.length; i++) {
    if (assignees.value[i].id > greatestId.value) {
      greatestId.value = assignees.value[i].id;
    }
  }
}
// Fetch all assignees on page load
onMounted(() => fetchAllAssignees());

</script>

<template>
  <div class="container-md">
    <div class="row py-3"></div>
    <!-- Edit Assignee Modal -->
    <div v-for="assignee in assignees" :key="assignee.id">
      <div class="modal" tabindex="-1" v-bind:id="'modal'+assignee.id.toString()">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Edit Assignee {{ assignee.id }}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form class="modal-body" id="20">
              <div class="mb-3">
                <label class="form-label">ID</label>
                <input class="form-control" type="number" disabled v-bind:placeholder="assignee.id.toString()">
              </div>
              <div class="mb-3">
                <label class="form-label">Prename</label>
                <input class="form-control" type="text" v-bind:value="assignee.prename"
                       v-bind:id="'editPrename'+assignee.id">
              </div>
              <div class="mb-3">
                <label class="form-label">Name</label>
                <input class="form-control" type="text" v-bind:value="assignee.name"
                       v-bind:id="'editName'+assignee.id">
              </div>
              <div class="mb-3">
                <label class="form-label">Email</label>
                <input class="form-control" type="text" v-bind:value="assignee.email"
                       v-bind:id="'editEmail'+assignee.id" aria-describedby="emailHelp">
                <div id="emailHelp" class="form-text">Email has to be valid and end on 'uni-stuttgart.de'</div>
              </div>
            </form>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
              <button type="button" class="btn btn-primary" @click="editAssignee(assignee.id)">Save changes</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- Create Assignee Modal -->
    <div class="modal" tabindex="-1" id="createAssignee">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Create new Assignee</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <form class="modal-body" id="21">
            <div class="mb-3">
              <label class="form-label">ID</label>
              <input class="form-control" type="number" disabled v-bind:placeholder="(greatestId+1).toString()">
            </div>
            <div class="mb-3">
              <label class="form-label">Prename</label>
              <input class="form-control" type="text" id="createPrename" v-bind:value="assignee.prename">
            </div>
            <div class="mb-3">
              <label class="form-label">Name</label>
              <input class="form-control" type="text" id="createName" v-bind:value="assignee.name">
            </div>
            <div class="mb-3">
              <label class="form-label">Email</label>
              <input class="form-control" type="email" id="createEmail" aria-describedby="emailHelp"
                     v-bind:value="assignee.email">
              <div id="emailHelp" class="form-text">Email has to be valid and end on 'uni-stuttgart.de'</div>
            </div>
          </form>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            <button type="button" class="btn btn-primary" @click="createAssignee">Save</button>
          </div>
        </div>
      </div>
    </div>
    <div class="alert alert-danger" v-if="assignees.length == 0" role="alert">
      No Assignees available on the server...
    </div>
    <div class="card mx-auto">
      <h3 class="card-title mx-3 my-3">Assignees</h3>
      <!-- Assignee Table -->
      <table class="table table-hover" style="text-align: left" id="assigneeTable">
        <thead>
        <tr>
          <th scope="col" style="width: 2%"></th>
          <th scope="col" style="width: 10%; text-align: center">ID</th>
          <th scope="col" style="width: 15%">Prename</th>
          <th scope="col" style="width: 15%">Name</th>
          <th scope="col" style="width: 23%">Email</th>
          <th scope="col" style="width: 10%"></th>
          <th scope="col" style="width: 10%"></th>

        </tr>
        </thead>
        <tbody>
        <tr v-for="assignee in assignees" :key="assignee.id">
          <td></td>
          <td style="text-align: center">{{ assignee.id }}</td>
          <td>{{ assignee.prename }}</td>
          <td>{{ assignee.name }}</td>
          <td>{{ assignee.email }}</td>
          <td>
            <button class="btn btn-danger btn-sm" @click="deleteAssignee(assignee.id)">
              <FontAwesomeIcon icon="trash"/>
              Delete
            </button>
          </td>
          <td>
            <button class="btn btn-secondary btn-sm" data-bs-toggle="modal"
                    v-bind:data-bs-target="'#modal'+assignee.id.toString()" @click="emptyAssignee">
              <FontAwesomeIcon icon="pencil"/>
              Edit
            </button>
          </td>
        </tr>
        </tbody>
      </table>
      <div class="card-footer">
        <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#createAssignee" @click="emptyAssignee">
          <FontAwesomeIcon icon="plus"/>
          Create new Assignee
        </button>
      </div>
    </div>
  </div>
</template>