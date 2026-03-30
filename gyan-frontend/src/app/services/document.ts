import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class DocumentService {

  private baseUrl = 'http://localhost:8080/documents';

  constructor(private http: HttpClient) {}

  getDocuments() {
    return this.http.get(this.baseUrl);
  }

  upload(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.baseUrl}/upload`, formData);
  }

  search(query: string) {
    return this.http.get(`${this.baseUrl}/search?q=${query}`);
  }

  ask(question: string) {
    return this.http.post('http://localhost:8080/ask', { question });
  }
}