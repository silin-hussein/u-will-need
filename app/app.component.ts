import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {VehicleComponent} from './car-image-upload/vehicle.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  imports: [
    VehicleComponent
  ],
  styleUrl: './app.component.css'
})
export class AppComponent {

}
