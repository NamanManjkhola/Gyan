import { Component } from '@angular/core';

@Component({
  selector: 'app-search',
  imports: [],
  templateUrl: './search.html',
  styleUrl: './search.scss',
})
export class Search {
  documentService: any;
  results: any;
  query: any;
    search() {
      this.documentService.search(this.query).subscribe((res: any) => {
      this.results = res;
    });
}

}
