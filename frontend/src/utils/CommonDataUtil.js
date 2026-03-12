export const adminMenuGroups = [
    {
        name: 'workflow',
        label: '工作流管理',
        items: [
            { key: 'flow', label: 'FLOW', path: '/admin/flow' },
            { key: 'template', label: 'TEMPLATE', path: '/admin/template' }
        ]
    },
    {
        name: 'model',
        label: '模型管理',
        items: [
            { key: 'agent', label: 'AGENT', path: '/admin/agent' },
            { key: 'client', label: 'CLIENT', path: '/admin/client' },
            { key: 'config', label: 'CONFIG', path: '/admin/config' }
        ]
    },
    {
        name: 'base',
        label: '服务管理',
        items: [
            { key: 'api', label: 'API', path: '/admin/api' },
            { key: 'model', label: 'MODEL', path: '/admin/model' },
            { key: 'mcp', label: 'MCP', path: '/admin/mcp' },
            { key: 'prompt', label: 'PROMPT', path: '/admin/prompt' },
            { key: 'advisor', label: 'ADVISOR', path: '/admin/advisor' }
        ]
    },
    {
        name: 'user',
        label: '用户管理',
        items: [
            { key: 'user', label: 'USER', path: '/admin/user' }
        ]
    },
    {
        name: 'other',
        label: '其他管理',
        items: [
            { key: 'task', label: 'TASK', path: '/admin/task' },
            { key: 'plaza', label: 'PLAZA', path: '/admin/plaza' },
            { key: 'session', label: 'SESSION', path: '/admin/session' }
        ]
    }
];
