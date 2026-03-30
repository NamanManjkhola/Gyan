import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { DocumentService } from '../../services/document';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule],
  templateUrl: './dashboard.html',
})
export class DashboardComponent implements OnInit {

  documents: any[] = [];

  constructor(private documentService: DocumentService, private router: Router) {}

  ngOnInit() {
    this.documentService.getDocuments().subscribe({
      next: (res: any) => {
        console.log("DOCUMENTS", res);
        this.documents = res.content || res; // handles pagination
      },
      error: (err) => {
        console.error("ERROR FETCHING DOCS", err);
      }
    });
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/']);
  }
}