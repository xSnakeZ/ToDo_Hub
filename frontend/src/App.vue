<script setup lang="ts">
import {RouterView} from 'vue-router'
import {Close, Toast, Toasts} from "agnostic-vue";
import {activeToasts} from "@/ts/toasts";
import 'agnostic-vue/dist/common.min.css';
import "agnostic-vue/dist/index.css";
import {onMounted, ref} from "vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import config from "@/config";
import {saveAs} from "file-saver";

let themeIsDark = ref(true);
let pathname = window.location.pathname;
function setTheme(){
  if (localStorage.getItem("mode") == null) {
    localStorage.setItem("mode", "dark")
  }
  document.documentElement.setAttribute('data-bs-theme', localStorage.getItem("mode")!)
}

/**
 * Change the theme of the page to either dark or light
 */
function changeTheme() {
  if (localStorage.getItem("mode") == "dark") {
    document.documentElement.setAttribute('data-bs-theme', 'light')
    localStorage.setItem("mode", "light")
    themeIsDark.value = false;
  } else if (localStorage.getItem("mode") == "light") {
    document.documentElement.setAttribute('data-bs-theme', 'dark')
    localStorage.setItem("mode", "dark")
    themeIsDark.value = true;
  }
}

/**
 * Export the todos as a csv file
 * @param option - The option to export the todos as (all, open, finished)
 */
function exportCSV(option: string){
  let filename = "todos.csv";
  if(option == "todos/open"){
    filename = "open_todos.csv";
  } else if (option == "todos/finished"){
    filename = "finished_todos.csv";
  }
  fetch(`${config.apiBaseUrl}/csv-downloads/${option}`)
      .then(response => response.blob())
      .then(data => {
        const blob = new Blob([data], { type: 'text/csv' });
        saveAs(blob, filename);
      })
}

onMounted(() => setTheme());
</script>

<template>
  <div id="app">
    <header>
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <nav class="navbar navbar-expand-lg bg-body-tertiary">
        <div class="container-fluid">
          <a class="navbar-brand"><font-awesome-icon :icon="['fas', 'pen-to-square']" style="color: #6f42c1" size="2xl" /></a>
          <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav" style="font-size: x-large">
              <li class="nav-item">
                <a class="nav-link active" href="/">ToDo's</a>
              </li>
              <li class="nav-item">
                <a class="nav-link active" href="/assignees">Assignees</a>
              </li>
            </ul>
            <div class="btn-group ms-auto" role="group" v-if="!(pathname.includes('assignees'))">
              <button type="button" class="btn btn-primary dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
                <font-awesome-icon :icon="['fas', 'download']" /> ToDo CSV-Export
              </button>
              <ul class="dropdown-menu">
                <li><button class="dropdown-item" type="button" @click="exportCSV('todos')">ALL</button></li>
                <li><button class="dropdown-item" type="button" @click="exportCSV('todos/open')">OPEN</button></li>
                <li><button class="dropdown-item" type="button" @click="exportCSV('todos/finished')">FINISHED</button></li>
              </ul>
            </div>
            <button class="btn btn-primary ms-5" @click="changeTheme" v-if="!(pathname.includes('assignees'))">
              <font-awesome-icon v-if="themeIsDark" :icon="['fas', 'sun']" />
              <font-awesome-icon v-else :icon="['fas', 'moon']" />
              Change theme
            </button>
            <button class="btn btn-primary ms-auto" @click="changeTheme" v-else>
              <font-awesome-icon v-if="themeIsDark" :icon="['fas', 'sun']" />
              <font-awesome-icon v-else :icon="['fas', 'moon']" />
              Change theme
            </button>
          </div>
        </div>
      </nav>
    </header>
    <div class="main">
      <RouterView/>
    </div>
  </div>

  <Toasts vertical-position="top" horizontal-position="end">
    <template v-for="toast of activeToasts" :key="toast.key">
      <Toast :type="toast.type" v-if="toast.type == 'success'" class="alert alert-border-left alert-success">
        <div class="flex-fill flex flex-column">
          <div class="flex">
            <h5 class="flex-fill"><strong>{{ toast.title }}</strong></h5>
            <Close @click="toast.close()"/>
          </div>
          <div class="flex">
            <font-awesome-icon :icon="toast.icon" size="xl" class="mie8 pbs2"></font-awesome-icon>
            <div class="flex-fill" style="font-size: large">
              {{ toast.message }}
            </div>
          </div>
        </div>
      </Toast>
      <Toast :type="toast.type" v-if="toast.type == 'error' || toast.type == 'warning'" class="alert alert-border-left alert-danger">
        <div class="flex-fill flex flex-column">
          <div class="flex">
            <h5 class="flex-fill"><strong>{{ toast.title }}</strong></h5>
            <Close @click="toast.close()"/>
          </div>
          <div class="flex">
            <font-awesome-icon :icon="toast.icon" size="xl" class="mie8 pbs2"></font-awesome-icon>
            <div class="flex-fill" style="font-size: large">
              {{ toast.message }}
            </div>
          </div>
        </div>
      </Toast>
      <Toast :type="toast.type" v-if="toast.type == 'info' || toast.type == 'dark'" class="alert alert-border-left alert-secondary">
        <div class="flex-fill flex flex-column">
          <div class="flex">
            <h5 class="flex-fill"><strong>{{ toast.title }}</strong></h5>
            <Close @click="toast.close()"/>
          </div>
          <div class="flex">
            <font-awesome-icon :icon="toast.icon" size="xl" class="mie8 pbs2"></font-awesome-icon>
            <div class="flex-fill" style="font-size: large">
              {{ toast.message }}
            </div>
          </div>
        </div>
      </Toast>
      <div class="mbe14"/>
    </template>
  </Toasts>
</template>

<style scoped>
.main {
  padding: 10px 20px;
}

</style>
