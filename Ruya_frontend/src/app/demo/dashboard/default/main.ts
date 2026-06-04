import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from '../../../app.component';
import { provideHttpClient } from '@angular/common/http';
import { importProvidersFrom } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(),
    importProvidersFrom(BrowserModule)
  ]
}).catch(err => console.error(err));