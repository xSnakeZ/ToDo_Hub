import { createRouter, createWebHistory } from 'vue-router'
import ToDoView from "@/todos/ToDoView.vue";
import AssigneeView from "@/assignees/AssigneeView.vue";


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'todos',
      component: ToDoView
    },
    {
      path: '/assignees',
      name: 'assignees',
      component: AssigneeView
    },
  ]
})

export default router
