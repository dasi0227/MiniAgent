import { createRouter, createWebHistory } from 'vue-router';
import Chat from '../components/Chat.vue';
import Work from '../components/Work.vue';
import Welcome from '../components/Welcome.vue';
import Studio from '../components/Studio.vue';
import Plaza from '../components/Plaza.vue';
import Repository from '../components/Repository.vue';
import Setting from '../components/Setting.vue';
import Detail from '../components/Detail.vue';
import Auth from '../components/Auth.vue';
import AdminAuth from '../components/AdminAuth.vue';
import AdminTable from '../components/AdminTable.vue';
import AdminConfig from '../components/AdminConfig.vue';
import AdminFlow from '../components/AdminFlow.vue';
import AdminCanvas from '../components/AdminCanvas.vue';
import AdminSession from '../components/AdminSession.vue';
import AdminDashboard from '../components/AdminDashboard.vue';
import NotFound from '../components/NotFound.vue';
import { getStoredAuth } from './pinia';

const routes = [
    {
        path: '/',
        redirect: '/chat'
    },
    {
        path: '/chat',
        name: 'chat',
        component: Chat
    },
    {
        path: '/welcome',
        name: 'welcome',
        component: Welcome
    },
    {
        path: '/work',
        name: 'work',
        component: Work
    },
    {
        path: '/studio',
        name: 'studio',
        component: Studio
    },
    {
        path: '/plaza',
        name: 'plaza',
        component: Plaza
    },
    {
        path: '/repository',
        name: 'repository',
        component: Repository
    },
    {
        path: '/detail/:templateId',
        name: 'detail',
        component: Detail
    },
    {
        path: '/setting',
        name: 'setting',
        component: Setting
    },
    {
        path: '/login',
        name: 'login',
        component: Auth,
        meta: {
            hideSidebar: true
        }
    },
    {
        path: '/register',
        name: 'register',
        component: Auth,
        meta: {
            hideSidebar: true
        }
    },
    {
        path: '/admin/login',
        name: 'admin-login',
        component: AdminAuth,
        meta: {
            hideSidebar: true
        }
    },
    {
        path: '/admin',
        redirect: '/admin/dashboard',
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/admin/dashboard',
        name: 'admin-dashboard',
        component: AdminDashboard,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/admin/config',
        name: 'admin-config',
        component: AdminConfig,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/admin/flow',
        name: 'admin-flow',
        component: AdminFlow,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/admin/canvas',
        name: 'admin-canvas',
        component: AdminCanvas,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/admin/session',
        name: 'admin-session',
        component: AdminSession,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/admin/:module',
        name: 'admin',
        component: AdminTable,
        meta: {
            requiresAuth: true,
            requiresAdmin: true,
            hideSidebar: true
        }
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'not-found',
        component: NotFound,
        meta: {
            hideSidebar: true
        }
    }
];

const router = createRouter({
    history: createWebHistory('/miniagent/'),
    routes
});

router.beforeEach((to, from, next) => {
    const auth = getStoredAuth();
    const isAdminRoute = to.path.startsWith('/admin');
    document.title = isAdminRoute ? 'Dasi MiniAgent 后台管理' : 'Dasi MiniAgent';

    // Global auth gate: if not logged in, allow only the login pages.
    if (!auth.token) {
        const allow = to.path === '/login' || to.path === '/register' || to.path === '/admin/login';
        if (!allow) {
            next({ path: '/login', replace: true });
            return;
        }
    }

    if (to.meta?.requiresAdmin && auth?.user?.role !== 'admin') {
        next({ path: '/login', replace: true });
        return;
    }
    next();
});

export default router;
