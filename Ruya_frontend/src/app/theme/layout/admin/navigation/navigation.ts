export interface NavigationItem {
  id: string;
  title: string;
  type: 'item' | 'collapse' | 'group';
  translate?: string;
  icon?: string;
  hidden?: boolean;
  url?: string;
  classes?: string;
  external?: boolean;
  target?: boolean;
  breadcrumbs?: boolean;
  children?: NavigationItem[];
  role?: string[];
  isMainParent?: boolean;
}

export const NavigationItems: NavigationItem[] = [
  {
    id: 'dashboard',
    title: 'Tableau de bord',
    type: 'group',
    icon: 'icon-navigation',
    children: [
      {
        id: 'default',
        title: 'Tableau de bord',
        type: 'item',
        classes: 'nav-item',
        url: '/default',
        icon: 'ti ti-dashboard',
        breadcrumbs: false
      }
    ]
  },
  {
    id: 'cheque',
    title: 'Chèque',
    type: 'collapse',
    icon: 'ti ti-currency-dollar',
    children: [
      { id: 'cheque-30', title: 'Fichier n° 30', type: 'item', url: '/cheque/30' },
      { id: 'cheque-31', title: 'Fichier n° 31', type: 'item', url: '/cheque/31' },
      { id: 'cheque-32', title: 'Fichier n° 32', type: 'item', url: '/cheque/32' },
      { id: 'cheque-33', title: 'Fichier n° 33', type: 'item', url: '/cheque/33' }
    ]
  },
  {
    id: 'prlv',
    title: 'Prélèvement',
    type: 'collapse',
    icon: 'ti ti-credit-card',
    children: [
      { id: 'prlv-20', title: 'Fichier n° 20', type: 'item', url: '/prlv/20' }
    ]
  },
  {
    id: 'effet',
    title: 'Effet',
    type: 'collapse',
    icon: 'ti ti-file-invoice',
    children: [
      { id: 'effet-40', title: 'Fichier n° 40', type: 'item', url: '/effet/40' },
      { id: 'effet-41', title: 'Fichier n° 41', type: 'item', url: '/effet/41' }
    ]
  },
  {
    id: 'virement',
    title: 'Virement',
    type: 'collapse',
    icon: 'ti ti-arrows-double-ne-sw',
    children: [
      { id: 'virement-10', title: 'Fichier n° 10', type: 'item', url: '/virement/10' }
    ]
  },
  {
    id: 'ajouter-fichier',
    title: 'Ajouter un fichier',
    type: 'item',
    icon: 'ti ti-plus',
    url: '/ajouter-fichier',
    classes: 'nav-item',
    breadcrumbs: false
  },
  {
    id: 'profile',
    title: 'Mon Profil',
    type: 'item',
    icon: 'ti ti-user-circle',
    url: '/profile',
    classes: 'nav-item',
    breadcrumbs: false
  },
  
  {
    id: 'gestion-utilisateur',
    title: 'Gestion Utilisateur',
    type: 'item',
    icon: 'ti ti-users',
    url: '/gestion-utilisateur',
    classes: 'nav-item',
    breadcrumbs: false,
    role: ['ADMIN'] // visible only to admin
  }
];
