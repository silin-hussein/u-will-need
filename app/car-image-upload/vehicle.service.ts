import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {map, Observable} from 'rxjs';
import { Vehicle } from './vehicle.model';

@Injectable({
  providedIn: 'root'
})
export class VehicleService {

  private baseUrl = 'http://localhost:8080/vehicle';

  constructor(private http: HttpClient) { }

  getVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(this.baseUrl);
  }

  addVehicle(vehicle: Vehicle) {
    return this.http.post(this.baseUrl, vehicle);
  }

  uploadImage(vehicleId: number, file: File) {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`http://localhost:8080/vehicle/${vehicleId}/image`, formData);
  }

  getVehicleImages(vehicleId: number): Observable<string[]> {
    return this.http.get<number[]>(`${this.baseUrl}/${vehicleId}/images/json`).pipe(
      map(ids => ids.map(id => `${this.baseUrl}/image?imageid=${id}`))
    );
  }

}
