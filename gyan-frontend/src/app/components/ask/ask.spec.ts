import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ask } from './ask';

describe('Ask', () => {
  let component: Ask;
  let fixture: ComponentFixture<Ask>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Ask]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Ask);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
