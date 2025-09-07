import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VehicleService } from './vehicle.service';
import { Vehicle } from './vehicle.model';

@Component({
  selector: 'app-vehicle',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vehicle.component.html',
  styleUrls: ['./vehicle.component.css']
})
export class VehicleComponent implements OnInit {

  vehicles: Vehicle[] = [];
  isLoading = true;
  errorMessage = '';

  // Felder für neues Fahrzeug
  newVehicle: Partial<Vehicle> = {
    brand: '',
    model: '',
    year: new Date().getFullYear()
  };

  // File-Uploads pro Fahrzeug
  selectedFiles: { [key: number]: File | null } = {};

  constructor(private vehicleService: VehicleService) {}

  ngOnInit(): void {
    this.loadVehicles();
  }

  loadVehicles() {
    this.vehicleService.getVehicles().subscribe({
      next: (data) => {
        this.vehicles = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Fehler beim Laden der Fahrzeuge';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  onAddVehicle() {
    if (!this.newVehicle.brand || !this.newVehicle.model || !this.newVehicle.year) {
      alert('Bitte Marke, Modell und Jahr angeben');
      return;
    }

    this.vehicleService.addVehicle(this.newVehicle as Vehicle).subscribe({
      next: () => {
        this.loadVehicles(); // Liste neu laden
        this.newVehicle = { brand: '', model: '', year: new Date().getFullYear() }; // Formular leeren
      },
      error: (err) => {
        console.error('Fehler beim Speichern', err);
        alert('Speichern fehlgeschlagen');
      }
    });
  }

  onFileSelected(event: any, vehicleId: number) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFiles[vehicleId] = file;
    }
  }

  onUpload(vehicleId: number) {
    const file = this.selectedFiles[vehicleId];
    if (!file) {
      alert('Bitte zuerst eine Datei auswählen');
      return;
    }

    this.vehicleService.uploadImage(vehicleId, file).subscribe({
      next: (res) => {
        console.log('Upload erfolgreich:', res);
        alert('Bild erfolgreich hochgeladen!');
        this.selectedFiles[vehicleId] = null;
      },
      error: (err) => {
        console.error('Fehler beim Upload:', err);
        alert('Upload fehlgeschlagen');
      }
    });
  }


  shownImages: { [key: number]: string[] } = {};

  toggleShowImages(vehicleId: number) {
    if (this.shownImages[vehicleId]) {
      delete this.shownImages[vehicleId]; // einklappen
    } else {
      this.vehicleService.getVehicleImages(vehicleId).subscribe({
        next: (images) => this.shownImages[vehicleId] = images,
        error: (err) => console.error('Fehler beim Laden der Bilder', err)
      });
    }
  }


  protected readonly alert = alert;
}
