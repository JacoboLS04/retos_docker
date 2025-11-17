// IMPORTANTE: Mocks DEBEN ir primero, antes de imports
jest.mock('fs', () => ({
  readFileSync: jest.fn((filePath: string) => {
    if (filePath.includes('welcomeEmail')) return 'Welcome {{nombre}}!';
    if (filePath.includes('loginAlert')) return 'Login detected for {{nombre}}';
    if (filePath.includes('resetPassword')) return 'Reset link: {{resetLink}}';
    if (filePath.includes('passwordChanged')) return 'Password changed for {{nombre}}';
    return 'Template content';
  }),
}));

jest.mock('url', () => ({
  fileURLToPath: jest.fn((url: string) => '/mocked/path/orchestrator.js'),
}));

jest.mock('uuid', () => ({
  v4: jest.fn(() => 'mock-uuid-123'),
}));

jest.mock('../src/rabbit.js');
jest.mock('../src/db.js');

import { renderTemplate, handleEvent } from '../src/orchestrator.js';
import * as rabbit from '../src/rabbit.js';
import * as db from '../src/db.js';

describe('Orchestrator Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('renderTemplate', () => {
    it('should replace variables in template', () => {
      const template = 'Hello {{name}}, your email is {{email}}';
      const variables = {
        name: 'John Doe',
        email: 'john@example.com',
      };

      const rendered = renderTemplate(template, variables);

      expect(rendered).toBe('Hello John Doe, your email is john@example.com');
    });

    it('should handle missing variables gracefully', () => {
      const template = 'Hello {{name}}, your role is {{role}}';
      const variables = {
        name: 'Jane',
      };

      const rendered = renderTemplate(template, variables);

      expect(rendered).toContain('Jane');
      expect(rendered).toContain('{{role}}'); // Unmatched variable stays
    });

    it('should handle empty template', () => {
      const rendered = renderTemplate('', {});
      expect(rendered).toBe('');
    });

    it('should replace multiple occurrences of same variable', () => {
      const template = 'Hello {{name}}, welcome {{name}}!';
      const variables = { name: 'Alice' };
      
      const rendered = renderTemplate(template, variables);
      
      expect(rendered).toBe('Hello Alice, welcome Alice!');
    });
  });

  describe('handleEvent', () => {
    it('should save event to database', async () => {
      const event = {
        id: 'evt-001',
        type: 'UNKNOWN_TYPE',
        userId: 123,
      };

      await handleEvent(event);

      expect(db.saveReceivedEvent).toHaveBeenCalledWith(event);
    });
  });
});
