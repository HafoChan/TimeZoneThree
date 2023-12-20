package com.demo.controllers.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.demo.DTO.ProductDTO;
import com.demo.DTO.UserDTO;
import com.demo.models.ERole;
import com.demo.models.Role;
import com.demo.models.User;
import com.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.demo.DTO.CategoryDTO;
import com.demo.service.CategoryService;
import com.demo.service.ProductService;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("")
public class HomeController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;
	
	@GetMapping("")
	public String homeRedirect () {
		return "redirect:/page/1";
	}
	@GetMapping("/page/{id}")
	public String home(Model model, HttpSession session, @PathVariable("id") Integer id) {

	    List<CategoryDTO> categoryDTOs = categoryService.getAllCategory()
	            .stream().map(e -> e.toDTO()).collect(Collectors.toList());

	    List<ProductDTO> listAllProductDTO = productService.getAllProduct().stream()
	            .map(e -> e.toDTO()).collect(Collectors.toList());

	    int pageNumber = (int) Math.ceil((double) listAllProductDTO.size() / 8);

	    if (id < 1 || id > pageNumber) {
	        // Xử lý khi id không hợp lệ
	        return "home_user"; // Hoặc chuyển hướng đến một trang lỗi khác
	    }

	    List<List<ProductDTO>> listPage = new ArrayList<>();

	    for (int i = 0; i < pageNumber; i++) {
	        int start = i * 8;
	        int end = Math.min((i + 1) * 8, listAllProductDTO.size());
	        List<ProductDTO> res = new ArrayList<>(listAllProductDTO.subList(start, end));
	        listPage.add(res);
	    }

	    Collections.shuffle(listPage.get(id - 1));

	    model.addAttribute("pageNumbers", pageNumber);
	    model.addAttribute("categoryDTOs", categoryDTOs);
	    model.addAttribute("productDTOs", listPage.get(id - 1));

	    return "home_user";
	}
	
	@GetMapping("/login")
	public String loginGet (Model model) {

		UserDTO userDTO = new UserDTO();

		List<CategoryDTO> categoryDTOs = categoryService.getAllCategory()
				.stream().map(e -> e.toDTO()).collect(Collectors.toList());

		List<ProductDTO> productDTOS = productService.getAllProduct().stream()
				.map(e -> e.toDTO()).collect(Collectors.toList());

		model.addAttribute("userDTO", userDTO);
		model.addAttribute("categoryDTOs", categoryDTOs);
		model.addAttribute("productDTOs", productDTOS);

		return "login-user";
	}
	
	@PostMapping("/login")
	public String loginPost (Model model, HttpSession session,
							 @ModelAttribute(name = "userDTO") UserDTO userDTO) {

		User user = userService.getUserByEmail(userDTO.getEmail());

		if (user == null){
			model.addAttribute("error", "Username or password is invalid");
			return "login-user";
		}

		if (user.getRole().getName().equals(ERole.ROLE_USER) && new BCryptPasswordEncoder().matches(userDTO.getPassword(), user.getPassword())){
			session.setAttribute("username", user.getUsername());
			return "redirect:/";
		}

		model.addAttribute("error", "Username or password is invalid");
		return "login-user";

	}

	@GetMapping("/register")
	public String registerGet (Model model) {

		UserDTO userDTO = new UserDTO();

		List<CategoryDTO> categoryDTOs = categoryService.getAllCategory()
				.stream().map(e -> e.toDTO()).collect(Collectors.toList());

		List<ProductDTO> productDTOS = productService.getAllProduct().stream()
				.map(e -> e.toDTO()).collect(Collectors.toList());

		model.addAttribute("userDTO", userDTO);
		model.addAttribute("categoryDTOs", categoryDTOs);
		model.addAttribute("productDTOs", productDTOS);

		return "register-user";
	}
	@PostMapping("/register")
	public String registerPost (Model model, @ModelAttribute(name = "userDTO") UserDTO userDTO) {

		userDTO.setRoleDTO((new Role(ERole.ROLE_USER)).toDTO());
		System.out.print(userDTO);
		User user0 = userService.getUserByEmail(userDTO.getEmail());
		User user1 = userService.getUserByUsername(userDTO.getUsername());
		
		if (user0 != null || user1 != null) {
			model.addAttribute("error", "Email or username is existed in system");
			return "register-user";
		}
		
		userService.createUser(userDTO.toModel());

		return "redirect:/login";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session){

		session.removeAttribute("username");

		return "redirect:/";
	}
	
	@GetMapping("/profile")
	public String login(HttpSession session){
		return "redirect:/";
	}
}
