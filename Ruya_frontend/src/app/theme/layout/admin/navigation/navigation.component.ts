
// navigation.component.ts
import { CommonModule } from '@angular/common';
import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { RouterModule } from '@angular/router';

import { NavContentComponent } from './nav-content/nav-content.component';
import { NavigationItem, NavigationItems } from './navigation';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [NavContentComponent, CommonModule, RouterModule],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent  {
  @Output() NavCollapsedMob = new EventEmitter<void>();
  @Output() SubmenuCollapse = new EventEmitter<void>();

  navCollapsedMob = false;
  windowWidth = window.innerWidth;
  themeMode!: string;
  navigations: NavigationItem[] = [];

 
  navCollapseMob() {
    if (this.windowWidth < 1025) {
      this.NavCollapsedMob.emit();
    }
  }

  navSubmenuCollapse() {
    document.querySelector('app-navigation.coded-navbar')?.classList.add('coded-trigger');
  }

 
}
